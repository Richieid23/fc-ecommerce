package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.category.CategoryRequest;
import id.web.fitrarizki.ecommerce.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);
    void deleteCategory(Long id);
}
