package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.category.CategoryResponse;
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
import id.web.fitrarizki.ecommerce.service.ProductService;
import id.web.fitrarizki.ecommerce.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public List<ProductResponse> getProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> ProductResponse.fromProductAndCategories(product, getProductCategories(product)))
                .toList();
    }

    @Override
    public PaginatedResponse<ProductResponse> getProducts(Integer page, Integer size, String[] sort, String name) {
        List<Sort.Order> orders = PageUtil.parseSortOrderRequest(sort);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(orders));

        Page<ProductResponse> products;
        if (name != null && !name.isEmpty()) {
            name = name.toLowerCase();
            products = productRepository.findByNameLike("%"+name+"%", pageRequest).map(product -> ProductResponse.fromProductAndCategories(product, getProductCategories(product)));
        } else {
            products = productRepository.findAll(pageRequest).map(product -> ProductResponse.fromProductAndCategories(product, getProductCategories(product)));
        }

        return PageUtil.getPaginatedResponse(products);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        List<CategoryResponse> categoryResponses = getProductCategories(product);

        return ProductResponse.fromProductAndCategories(product, categoryResponses);
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

        return ProductResponse.fromProductAndCategories(product, categories.stream().map(CategoryResponse::fromCategory)
                .toList());
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
        List<Long> productCategoryIds = productCategoryRepository.findCategoriesByProductId(product.getId())
                .stream()
                .map(productCategory -> productCategory.getId().getCategoryId())
                .toList();
        return categoryRepository.findAllById(productCategoryIds)
                .stream()
                .map(CategoryResponse::fromCategory).toList();
    }
}
