package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.SearchResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductSearchRequest;
import id.web.fitrarizki.ecommerce.model.ActivityType;

import java.util.List;

public interface SearchService {
    SearchResponse<ProductResponse> searchProducts(ProductSearchRequest productSearchRequest);
    SearchResponse<ProductResponse> similarProducts(Long productId);
    SearchResponse<ProductResponse> userRecommendation(Long userId, ActivityType activityType);
    List<String> getAutocomplete(String query);
    List<String> getNgramAutocomplete(String query);
    List<String> getFuzzyAutocomplete(String query);
    List<String> combinedAutocomplete(String query);
}
