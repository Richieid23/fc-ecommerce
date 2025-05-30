package id.web.fitrarizki.ecommerce.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import id.web.fitrarizki.ecommerce.dto.product.ProductDocument;
import id.web.fitrarizki.ecommerce.model.Category;
import id.web.fitrarizki.ecommerce.model.Product;
import id.web.fitrarizki.ecommerce.repository.CategoryRepository;
import id.web.fitrarizki.ecommerce.repository.ProductCategoryRepository;
import id.web.fitrarizki.ecommerce.repository.ProductRepository;
import id.web.fitrarizki.ecommerce.service.BulkReindexService;
import id.web.fitrarizki.ecommerce.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkReindexServiceImpl implements BulkReindexService {
    private final ElasticsearchClient elasticsearchClient;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductIndexService productIndexService;

    private final int BATCH_SIZE = 100;

    @Transactional(readOnly = true)
    @Override
    @Async
    public void reindexAllProducts() throws IOException {
        long startTime = System.currentTimeMillis();
        AtomicLong totalIndexed = new AtomicLong(0);
        List<ProductDocument> productDocuments = new ArrayList<>(BATCH_SIZE);

        try (Stream<Product> products = productRepository.streamAll()) {
            products.forEach(product -> {
                List<Long> categoryIds = productCategoryRepository.findCategoriesByProductId(product.getId()).stream().map(productCategory -> productCategory.getId().getCategoryId()).toList();
                List<Category> categories = categoryRepository.findAllById(categoryIds);
                ProductDocument productDocument = ProductDocument.fromProductAndCategories(product, categories);
                productDocuments.add(productDocument);

                if (productDocuments.size() >= BATCH_SIZE) {
                    try {
                        totalIndexed.addAndGet(indexBatch(productDocuments));
                    } catch (IOException e) {
                        log.error("Error while indexing products: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                    productDocuments.clear();
                }
            });
        }

        if (!productDocuments.isEmpty()) {
            totalIndexed.addAndGet(indexBatch(productDocuments));
        }

        long endtime = System.currentTimeMillis();
        log.info("Reindex complete. Indexed {} products in {} ms", totalIndexed, (endtime - startTime));
    }

    private long indexBatch(List<ProductDocument> productDocuments) throws IOException {
        BulkRequest.Builder builder = new BulkRequest.Builder();

        for (ProductDocument productDocument : productDocuments) {
            builder.operations(ops -> ops.update(upd -> upd.index(productIndexService.getProductIndexName()).id(productDocument.getId()).action(act -> act.docAsUpsert(true).doc(productDocument))));
        }

        BulkResponse response = elasticsearchClient.bulk(builder.build());
        if (response.errors()) {
            log.error("Error while performing bulk index operation");
            for (BulkResponseItem item : response.items()) {
                if (item.error() != null) {
                    log.error("Bulk index error: {}", item.error().reason());
                }
            }
        }

        return productDocuments.size();
    }
}
