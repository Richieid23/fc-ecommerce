package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.SearchResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductRequest;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductSearchRequest;
import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.ProductService;
import id.web.fitrarizki.ecommerce.service.SearchService;
import id.web.fitrarizki.ecommerce.service.UserActivityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class ProductController {
    private final ProductService productService;
    private final SearchService searchService;
    private final UserActivityService userActivityService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse<ProductResponse>> searchProduct(@RequestBody ProductSearchRequest productRequest) {
        return ResponseEntity.ok(searchService.searchProducts(productRequest));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(productService.getProducts(page, size, sort, name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ProductResponse productResponse = productService.getProductById(id);
        if (!productResponse.getUserId().equals(userInfo.getUser().getId())) {
            userActivityService.trackProductView(id, userInfo.getUser().getId());
        }
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<SearchResponse<ProductResponse>> similarProduct(@PathVariable Long id) {
        return ResponseEntity.ok(searchService.similarProducts(id));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<SearchResponse<ProductResponse>> recommendations(@RequestParam(value = "user_activity", defaultValue = "VIEW") ActivityType activityType) {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(searchService.userRecommendation(userInfo.getUser().getId(), activityType));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProductById(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, productRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
