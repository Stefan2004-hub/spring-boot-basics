package com.interview.demo.controller;

import com.interview.demo.dto.CategoryResponse;
import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.repository.projection.CategoryProjection;
import com.interview.demo.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  public List<CategoryResponse> getAllCategories() {
    return categoryService.getAllCategories();
  }

  @GetMapping("/details")
  public List<CategoryProjection> getAllCategoriesDetails() {
    return categoryService.getAllCategoriesDetails();
  }

  @PostMapping
  public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    return categoryService.createCategory(request);
  }

  @PutMapping("/{id}")
  public CategoryResponse updateCategory(
      @PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) {
    return categoryService.updateCategory(id, request);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return Map.of("message", "Category deleted successfully");
  }
}
