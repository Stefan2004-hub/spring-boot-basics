package com.interview.demo.controller;

import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.entity.Category;
import com.interview.demo.service.CategoryService;
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

  @PostMapping
  public Category createCategory(@RequestBody CreateCategoryRequest request) {
    return categoryService.createCategory(request);
  }
}
