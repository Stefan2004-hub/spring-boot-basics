package com.interview.demo.service;

import com.interview.demo.dto.CategoryProductSummaryResponse;
import com.interview.demo.dto.CategoryResponse;
import com.interview.demo.dto.CategoryResponseDetails;
import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.entity.Category;
import com.interview.demo.exception.ConflictException;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.exception.ValidationException;
import com.interview.demo.repository.CategoryRepository;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService implements CategoryServiceContract {
  private final CategoryRepository categoryRepository;
  private final ProductManagerContract productManager;

  public CategoryService(
      CategoryRepository categoryRepository, ProductManagerContract productManager) {
    this.categoryRepository = categoryRepository;
    this.productManager = productManager;
  }

  @Transactional
  public CategoryResponse createCategory(CreateCategoryRequest request) {
    if (request.name() == null || request.name().isBlank()) {
      throw new ValidationException("Category name is required");
    }
    assertCategoryNameAvailable(request.name(), null);
    Category category = new Category();
    category.setName(request.name().trim());
    return toResponse(categoryRepository.save(category));
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> getAllCategories() {
    return categoryRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<CategoryResponseDetails> getAllCategoriesDetails(int page, int size) {
    if (page < 1) {
      throw new ValidationException("page must be at least 1");
    }
    if (size < 1 || size > 100) {
      throw new ValidationException("size must be between 1 and 100");
    }

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
    List<Category> categories = categoryRepository.findAll(pageable).getContent();

    if (categories.isEmpty()) {
      return List.of();
    }

    List<Long> categoryIds = categories.stream().map(Category::getId).toList();
    Map<Long, List<CategoryProductSummaryResponse>> productsByCategoryId =
        productManager.getProductSummaries(categoryIds);

    return categories.stream()
        .map(
            category ->
                new CategoryResponseDetails(
                    category.getId(),
                    category.getName(),
                    productsByCategoryId.getOrDefault(category.getId(), List.of())))
        .toList();
  }

  private CategoryResponse toResponse(Category category) {
    return new CategoryResponse(category.getId(), category.getName());
  }

  @Transactional
  public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
    if (request.name() == null || request.name().isBlank()) {
      throw new ValidationException("Category name is required");
    }
    Category category = getById(id);
    assertCategoryNameAvailable(request.name(), id);
    category.setName(request.name().trim());
    return toResponse(categoryRepository.save(category));
  }

  @Transactional
  public void deleteCategory(Long id) {
    Category category = getById(id);
    try {
      categoryRepository.delete(category);
      categoryRepository.flush();
    } catch (DataIntegrityViolationException ex) {
      throw new ConflictException("Category is in use and cannot be deleted: " + id);
    }
  }

  public Category getById(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
  }

  private void assertCategoryNameAvailable(String name, Long currentCategoryId) {
    categoryRepository
        .findByNameIgnoreCase(name)
        .ifPresent(
            existing -> {
              if (currentCategoryId == null || !existing.getId().equals(currentCategoryId)) {
                throw new ConflictException("Category already exists: " + name);
              }
            });
  }
}
