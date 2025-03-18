package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.category.CategoryRequest;
import id.web.fitrarizki.ecommerce.dto.category.CategoryResponse;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.Category;
import id.web.fitrarizki.ecommerce.repository.CategoryRepository;
import id.web.fitrarizki.ecommerce.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::fromCategory)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id).map(CategoryResponse::fromCategory)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = categoryRepository.save(Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .build());

        return CategoryResponse.fromCategory(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        categoryRepository.save(category);

        return CategoryResponse.fromCategory(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }
}
