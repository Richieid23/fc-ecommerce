package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductRequest;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getProducts();
    PaginatedResponse<ProductResponse> getProducts(Integer page, Integer size, String[] sort, String name);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse updateProduct(Long id, ProductRequest productRequest);
    void deleteProductById(Long id);
}
