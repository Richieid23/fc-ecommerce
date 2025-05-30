package id.web.fitrarizki.ecommerce.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import id.web.fitrarizki.ecommerce.dto.SearchResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductDocument;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductSearchRequest;
import id.web.fitrarizki.ecommerce.service.ProductIndexService;
import id.web.fitrarizki.ecommerce.service.ProductService;
import id.web.fitrarizki.ecommerce.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ProductIndexService productIndexService;
    private final ElasticsearchClient elasticsearchClient;
    private final ProductService productService;

    @Override
    public SearchResponse<ProductResponse> searchProducts(ProductSearchRequest productSearchRequest) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // full text search on name & descroption
        if (productSearchRequest.getQuery() != null && !productSearchRequest.getQuery().isEmpty()) {
            boolQuery.must(MultiMatchQuery.of(mm -> mm.fields("name", "description").query(productSearchRequest.getQuery()))._toQuery());
        }

        // category filter
        if (productSearchRequest.getCategory() != null && !productSearchRequest.getCategory().isEmpty()) {
            Query nestedQuery = NestedQuery.of(n -> n.path("categories").query(q -> q.term(t -> t.field("categories.name.keyword").value(productSearchRequest.getCategory()))))._toQuery();
            boolQuery.filter(nestedQuery);
        }

        // price range filter
        if (productSearchRequest.getMinPrice() != null || productSearchRequest.getMaxPrice() != null) {
            RangeQuery.Builder rangeQuery = new RangeQuery.Builder().field("price");

            if (productSearchRequest.getMinPrice() != null) {
                rangeQuery.gte(JsonData.of(productSearchRequest.getMinPrice()));
            }

            if (productSearchRequest.getMaxPrice() != null) {
                rangeQuery.lte(JsonData.of(productSearchRequest.getMinPrice()));
            }

            boolQuery.filter(rangeQuery.build()._toQuery());
        }

        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder().index(productIndexService.getProductIndexName()).query(boolQuery.build()._toQuery());

        // add sorting
        searchRequestBuilder.sort(s -> s.field(f -> f.field(productSearchRequest.getSortBy()).order("asc".equals(productSearchRequest.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc)));

        // add pagination
        searchRequestBuilder.from((productSearchRequest.getPage()-1) * productSearchRequest.getSize()).size(productSearchRequest.getSize());

        searchRequestBuilder.aggregations("categories", a -> a.nested(n -> n.path("categories")).aggregations("category_names", sa -> sa.terms(t -> t.field("categories.name.keyword")))).from(productSearchRequest.getPage() - 1);

        SearchRequest searchRequest = searchRequestBuilder.build();
        SearchResponse<ProductResponse> response = new SearchResponse<>();
        try {
            co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result = elasticsearchClient.search(searchRequest, ProductDocument.class);

            List<ProductResponse> productResponses = result.hits().hits().stream().filter(productDocumentHit -> productDocumentHit != null && productDocumentHit.id() != null).map(productDocumentHit -> Long.parseLong(productDocumentHit.id())).map(productService::getProductById).toList();

            response.setData(productResponses);
            if (result.hits().total() != null) {
                response.setTotalHits(result.hits().total().value());
            }

            if (result.aggregations() != null) {
                Map<String, List<SearchResponse.FacetEntry>> facets = new HashMap<>();
                var categoriesAgg = result.aggregations().get("categories");
                if (categoriesAgg != null && categoriesAgg.nested() != null) {
                    var categoriesNameAgg = categoriesAgg.nested().aggregations().get("category_names");
                    if (categoriesNameAgg != null && categoriesNameAgg.sterms() != null) {
                        List<SearchResponse.FacetEntry> categoryFacets = categoriesNameAgg.sterms().buckets().array().stream().map(bucket -> new SearchResponse.FacetEntry(bucket.key().stringValue(), bucket.docCount())).toList();
                        facets.put("categories", categoryFacets);
                    }
                }
                response.setFacets(facets);
            }

        } catch (IOException e) {
            log.error("Error while searching products: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }
}
