package com.interview.demo.service;

import com.interview.demo.dto.CategoryResponse;
import com.interview.demo.dto.CategoryResponseDetails;
import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.entity.Category;
import java.util.List;

public interface CategoryServiceContract {
  CategoryResponse createCategory(CreateCategoryRequest request);

  List<CategoryResponse> getAllCategories();

  CategoryResponse getCategoryById(Long id);

  List<CategoryResponseDetails> getAllCategoriesDetails(int page, int size);

  CategoryResponse updateCategory(Long id, CreateCategoryRequest request);

  void deleteCategory(Long id);

  Category getById(Long id);
}
