package id.web.fitrarizki.ecommerce.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import id.web.fitrarizki.ecommerce.dto.product.ProductDocument;
import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.model.Category;
import id.web.fitrarizki.ecommerce.model.Product;
import id.web.fitrarizki.ecommerce.service.CategoryService;
import id.web.fitrarizki.ecommerce.service.ProductIndexService;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductIndexServiceImpl implements ProductIndexService {

    private final ElasticsearchClient elasticsearchClient;
    private final CategoryService categoryService;
    private final Retry elasticsearchIndexRetrier;

    private final String INDEX_NAME = "products";

    @Override
    @Async
    public void reIndexProducts(Product product) {
        List<Category> categories = categoryService.getProductCategories(product.getId());
        ProductDocument productDocument = ProductDocument.fromProductAndCategories(product, categories);
        IndexRequest<ProductDocument> request = IndexRequest.of(builder -> builder.index(INDEX_NAME).id(String.valueOf(product.getId())).document(productDocument));

        try {
            elasticsearchIndexRetrier.executeCallable(() -> {
                elasticsearchClient.index(request);
                return null;
            });
        } catch (IOException e) {
            log.error("Error re-indexing product: {}", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async
    public void deleteProduct(Product product) {
        DeleteRequest request = DeleteRequest.of(builder -> builder.index(INDEX_NAME).id(String.valueOf(product.getId())));

        try {
            elasticsearchIndexRetrier.executeCallable(() -> {
                elasticsearchClient.delete(request);
                return null;
            });
        } catch (IOException e) {
            log.error("Error re-indexing product: {}", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProductIndexName() {
        return INDEX_NAME;
    }

    @Override
    public void reindexProductActivity(Long productId, ActivityType activityType, Long value) {
        final String field = (activityType.equals(ActivityType.VIEW)) ? "view_count" : "purchase_count";

        UpdateRequest<ProductDocument, Map<String, Object>> request = UpdateRequest.of(builder -> builder.index(INDEX_NAME).id(productId.toString()).doc(Map.of(field, value)));

        try {
            elasticsearchIndexRetrier.executeCallable(() -> {
                elasticsearchClient.update(request, ProductDocument.class);
                return null;
            });
        } catch (IOException e) {
            log.error("Error re-indexing product activity: {}", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
