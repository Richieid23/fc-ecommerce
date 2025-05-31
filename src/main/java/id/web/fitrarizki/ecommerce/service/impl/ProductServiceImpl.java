package id.web.fitrarizki.ecommerce.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.category.CategoryResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductReindex;
import id.web.fitrarizki.ecommerce.dto.product.ProductRequest;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.Category;
import id.web.fitrarizki.ecommerce.model.Product;
import id.web.fitrarizki.ecommerce.model.ProductCategory;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.repository.CategoryRepository;
import id.web.fitrarizki.ecommerce.repository.ProductCategoryRepository;
import id.web.fitrarizki.ecommerce.repository.ProductRepository;
import id.web.fitrarizki.ecommerce.service.*;
import id.web.fitrarizki.ecommerce.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final CacheService cacheService;
    private final RateLimitingService rateLimitingService;
    private final ProductIndexService productIndexService;
    private final ProductReindexProducer productReindexProducer;

    private final String PRODUCT_CACHE_KEY = "products:";
    private final String PRODUCT_CATEGORY_CACHE_KEY = "product:categories:";

    @Override
    public List<ProductResponse> getProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> ProductResponse.fromProductAndCategories(product, getProductCategories(product)))
                .toList();
    }

    @Override
    public PaginatedResponse<ProductResponse> getProducts(Integer page, Integer size, String[] sort, String name) {
        return rateLimitingService.executeWithRateLimit("product_listing", () -> {
            List<Sort.Order> orders = PageUtil.parseSortOrderRequest(sort);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(orders));

            Page<ProductResponse> products;
            if (name != null && !name.isEmpty()) {
                products = productRepository.findByNameLike("%"+name.toLowerCase()+"%", pageRequest).map(product -> ProductResponse.fromProductAndCategories(product, getProductCategories(product)));
            } else {
                products = productRepository.findAll(pageRequest).map(product -> ProductResponse.fromProductAndCategories(product, getProductCategories(product)));
            }

            return PageUtil.getPaginatedResponse(products);
        });
    }

    @Override
    public ProductResponse getProductById(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY+id;
        Optional<ProductResponse> cachedProduct = cacheService.get(cacheKey, ProductResponse.class);
        if (cachedProduct.isPresent()) {
            return cachedProduct.get();
        }

        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        List<CategoryResponse> categoryResponses = getProductCategories(product);

        ProductResponse productResponse = ProductResponse.fromProductAndCategories(product, categoryResponses);
        cacheService.set(cacheKey, productResponse);
        return productResponse;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        List<Category> categories = categoryRepository.findAllById(productRequest.getCategoryIds());

        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Product product = productRepository.save(Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .weight(productRequest.getWeight())
                .userId(userInfo.getUser().getId())
                .build());

        List<ProductCategory> productCategoryList = generateProductCategories(product, categories);

        productCategoryRepository.saveAll(productCategoryList);

        ProductResponse productResponse = ProductResponse.fromProductAndCategories(product, categories.stream().map(CategoryResponse::fromCategory).toList());
        cacheService.set(PRODUCT_CACHE_KEY+product.getId(), productResponse);
//        productIndexService.reIndexProducts(product);
        productReindexProducer.publishProductReindex(ProductReindex.builder().action("REINDEX").productId(product.getId()).build());
        return productResponse;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        List<Category> categories = categoryRepository.findAllById(productRequest.getCategoryIds());

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setWeight(productRequest.getWeight());
        productRepository.save(product);

        List<ProductCategory> productCategories = productCategoryRepository.findCategoriesByProductId(id);
        productCategoryRepository.deleteAll(productCategories);

        List<ProductCategory> productCategoryList = generateProductCategories(product, categories);

        productCategoryRepository.saveAll(productCategoryList);

        cacheService.evict(PRODUCT_CACHE_KEY+id);
//        productIndexService.reIndexProducts(product);
        productReindexProducer.publishProductReindex(ProductReindex.builder().action("REINDEX").productId(product.getId()).build());
        return ProductResponse.fromProductAndCategories(product, categories.stream().map(CategoryResponse::fromCategory)
                .toList());
    }

    @Override
    @Transactional
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        List<ProductCategory> productCategoryList = productCategoryRepository.findCategoriesByProductId(id);

        productCategoryRepository.deleteAll(productCategoryList);
        productRepository.delete(product);
        cacheService.evict(PRODUCT_CACHE_KEY+id);
//        productIndexService.deleteProduct(product);
        productReindexProducer.publishProductReindex(ProductReindex.builder().action("DELETE").productId(product.getId()).build());
    }

    @Override
    public Product get(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    private List<ProductCategory> generateProductCategories(Product product, List<Category> categories) {
        return categories.stream()
                .map(category -> ProductCategory.builder()
                        .id(ProductCategory.ProductCategoryId.builder()
                                .productId(product.getId())
                                .categoryId(category.getId())
                                .build())
                        .build())
                .toList();
    }

    private List<CategoryResponse> getProductCategories(Product product) {
        String cacheKey = PRODUCT_CATEGORY_CACHE_KEY+product.getId();
        Optional<List<CategoryResponse>> categoryResponsesOpt = cacheService.get(cacheKey, new TypeReference<List<CategoryResponse>>() {});
        if (categoryResponsesOpt.isPresent()) {
            return categoryResponsesOpt.get();
        }

        List<Long> productCategoryIds = productCategoryRepository.findCategoriesByProductId(product.getId())
                .stream()
                .map(productCategory -> productCategory.getId().getCategoryId())
                .toList();
        List<CategoryResponse> categoryResponses = categoryRepository.findAllById(productCategoryIds)
                .stream()
                .map(CategoryResponse::fromCategory).toList();
        cacheService.set(cacheKey, categoryResponses);
        return categoryResponses;
    }
}
