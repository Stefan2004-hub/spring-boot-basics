package com.interview.demo.service;

import com.interview.demo.dto.CategoryResponse;
import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.entity.Category;
import com.interview.demo.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  public CategoryResponse createCategory(CreateCategoryRequest request) {
    if (request.name() == null || request.name().isBlank()) {
      throw new IllegalArgumentException("Category name is required");
    }
    categoryRepository
        .findByNameIgnoreCase(request.name())
        .ifPresent(existing -> {
          throw new IllegalArgumentException("Category already exists: " + request.name());
    });
    Category category = new Category();
    category.setName(request.name().trim());
    return toResponse(categoryRepository.save(category));
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> getAllCategories() {
    return categoryRepository.findAll().stream().map(this::toResponse).toList();
  }

  private CategoryResponse toResponse(Category category) {
    return new CategoryResponse(category.getId(), category.getName());
  }
}
