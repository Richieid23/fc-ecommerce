package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.SearchResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductSearchRequest;

public interface SearchService {
    SearchResponse<ProductResponse> searchProducts(ProductSearchRequest productSearchRequest);
}
