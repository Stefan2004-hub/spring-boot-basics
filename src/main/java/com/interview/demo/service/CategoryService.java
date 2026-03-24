package com.interview.demo.service;

import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.entity.Category;
import com.interview.demo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public Category createCategory(CreateCategoryRequest request) {
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
    return categoryRepository.save(category);
  }
}
