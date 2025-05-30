package id.web.fitrarizki.ecommerce.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import id.web.fitrarizki.ecommerce.dto.SearchResponse;
import id.web.fitrarizki.ecommerce.dto.category.CategoryResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductDocument;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductSearchRequest;
import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.model.UserActivity;
import id.web.fitrarizki.ecommerce.service.ProductIndexService;
import id.web.fitrarizki.ecommerce.service.ProductService;
import id.web.fitrarizki.ecommerce.service.SearchService;
import id.web.fitrarizki.ecommerce.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ProductIndexService productIndexService;
    private final ElasticsearchClient elasticsearchClient;
    private final ProductService productService;
    private final UserActivityService userActivityService;

    private final int SIMILAR_PRODUCT_COUNT = 10;
    private final int USER_RECOMMENDATION_LIMIT = 10;

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

        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f -> f.query(q -> q.bool(boolQuery.build())).functions(FunctionScore.of(fs -> fs.fieldValueFactor(fvf -> fvf.field("viewCount").factor(1.0).modifier(FieldValueFactorModifier.Log1p))), FunctionScore.of(fs -> fs.fieldValueFactor(fvf -> fvf.field("purchaseCount").factor(2.0).modifier(FieldValueFactorModifier.Log1p)))).boostMode(FunctionBoostMode.Multiply).scoreMode(FunctionScoreMode.Sum));

        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder().index(productIndexService.getProductIndexName()).query(functionScoreQuery._toQuery());

        // add sorting
        searchRequestBuilder.sort(s -> s.field(f -> f.field(productSearchRequest.getSortBy()).order("asc".equals(productSearchRequest.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc)));

        // add pagination
        searchRequestBuilder.from((productSearchRequest.getPage()-1) * productSearchRequest.getSize()).size(productSearchRequest.getSize());

        searchRequestBuilder.aggregations("categories", a -> a.nested(n -> n.path("categories")).aggregations("category_names", sa -> sa.terms(t -> t.field("categories.name.keyword")))).from(productSearchRequest.getPage() - 1);

        SearchRequest searchRequest = searchRequestBuilder.build();
        SearchResponse<ProductResponse> response = new SearchResponse<>();
        try {
            co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result = elasticsearchClient.search(searchRequest, ProductDocument.class);

            return mapSearchResult(result);
        } catch (IOException e) {
            log.error("Error while searching products: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public SearchResponse<ProductResponse> similarProducts(Long productId) {
        ProductResponse sourceProduct = productService.getProductById(productId);

        List<String> categoriesName = sourceProduct.getCategories().stream().map(CategoryResponse::getName).toList();

        MoreLikeThisQuery moreLikeThisQuery = MoreLikeThisQuery.of(m -> m.fields("name", "description")
                .like(l ->
                        l.document(d ->
                                d.index(productIndexService.getProductIndexName()).id(productId.toString())))
                .minTermFreq(1)
                .maxQueryTerms(12)
                .minDocFreq(1));

        List<FieldValue> categoryNameFieldValues = categoriesName.stream().map(FieldValue::of).toList();

        NestedQuery nestedQuery = NestedQuery.of(n -> n.path("categories")
                .query(q ->
                        q.terms(t ->
                                t.field("categories.name").terms(t2 ->
                                        t2.value(categoryNameFieldValues))))
                .scoreMode(ChildScoreMode.Avg));

        BoolQuery boolQuery = BoolQuery.of(b ->
                b.must(m ->
                        m.moreLikeThis(moreLikeThisQuery)).should(s -> s.nested(nestedQuery)));

        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f ->
                f.query(q -> q.bool(boolQuery))
                        .functions(
                                FunctionScore.of(fs ->
                                        fs.fieldValueFactor(fvf ->
                                                fvf.field("viewCount")
                                                        .factor(1.0)
                                                        .modifier(FieldValueFactorModifier.Log1p))),
                                FunctionScore.of(fs ->
                                        fs.fieldValueFactor(fvf ->
                                                fvf.field("purchaseCount")
                                                        .factor(2.0)
                                                        .modifier(FieldValueFactorModifier.Log1p))))
                        .boostMode(FunctionBoostMode.Multiply)
                        .scoreMode(FunctionScoreMode.Sum));

        try {
            co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result = elasticsearchClient.search(s -> s.index(productIndexService.getProductIndexName()).query(q -> q.functionScore(functionScoreQuery)).size(SIMILAR_PRODUCT_COUNT), ProductDocument.class);

            return mapSearchResult(result);
        } catch (IOException e) {
            log.error("Error while searching products: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public SearchResponse<ProductResponse> userRecommendation(Long userId, ActivityType activityType) {
        if (!List.of(ActivityType.VIEW, ActivityType.PURCHASE).contains(activityType)) {
            SearchResponse<ProductResponse> response = new SearchResponse<>();
            response.setTotalHits(0L);
            response.setFacets(Map.of());
            response.setData(List.of());
            return response;
        }

        List<UserActivity> userActivities;

        if (activityType.equals(ActivityType.PURCHASE)) {
            userActivities = userActivityService.getLastMonthUserPurchase(userId);
        } else {
            userActivities = userActivityService.getLastMonthUserView(userId);
        }

        List<Long> productIds = getTopProductIds(userActivities);

        return productRecommendationOnActivityType(productIds, activityType);
    }

    private SearchResponse<ProductResponse> productRecommendationOnActivityType(List<Long> productIds, ActivityType activityType) {
        List<Like> likes = productIds.stream().map(productId -> Like.of(builder -> builder.text(productId.toString()))).toList();

        MoreLikeThisQuery moreLikeThisQuery = MoreLikeThisQuery.of(m -> m.fields("name", "description").like(likes).minTermFreq(1).maxQueryTerms(12).minDocFreq(1));

        String fieldName = activityType.equals(ActivityType.PURCHASE) ? "purchaseCount" : "viewCount";
        double factoryValues = activityType.equals(ActivityType.PURCHASE) ? 2.0 : 1.0;

        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f -> f.query(q -> q.moreLikeThis(moreLikeThisQuery)).functions(FunctionScore.of(fs -> fs.fieldValueFactor(fvf -> fvf.field(fieldName).factor(factoryValues).modifier(FieldValueFactorModifier.Log1p)))).boostMode(FunctionBoostMode.Multiply).scoreMode(FunctionScoreMode.Sum));

        co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result;

        try {
            result = elasticsearchClient.search(s -> s.index(productIndexService.getProductIndexName()).query(q -> q.functionScore(functionScoreQuery)).size(USER_RECOMMENDATION_LIMIT), ProductDocument.class);
        } catch (IOException e) {
            log.error("Error while searching products: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return mapSearchResult(result);
    }

    private List<Long> getTopProductIds(List<UserActivity> activities) {
        return activities.stream().collect(Collectors.groupingBy(UserActivity::getProductId, Collectors.counting())).entrySet().stream().sorted(Map.Entry.<Long, Long>comparingByValue().reversed()).limit(5).map(Map.Entry::getKey).toList();
    }

    private SearchResponse<ProductResponse> mapSearchResult(co.elastic.clients.elasticsearch.core.SearchResponse<ProductDocument> result) {
        List<ProductResponse> productResponses = result.hits().hits().stream().filter(productDocumentHit -> productDocumentHit != null && productDocumentHit.id() != null).map(productDocumentHit -> Long.parseLong(productDocumentHit.id())).map(productService::getProductById).toList();

        SearchResponse<ProductResponse> response = new SearchResponse<>();
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

        return response;
    }
}
