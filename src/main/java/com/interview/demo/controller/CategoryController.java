package com.interview.demo.controller;

import com.interview.demo.dto.CategoryResponse;
import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

  @PostMapping
  public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    return categoryService.createCategory(request);
  }
}
