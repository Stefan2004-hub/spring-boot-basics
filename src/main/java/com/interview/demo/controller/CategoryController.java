package com.interview.demo.controller;

import com.interview.demo.dto.CategoryResponse;
import com.interview.demo.dto.CategoryResponseDetails;
import com.interview.demo.dto.CreateCategoryRequest;
import com.interview.demo.hateoas.CategoryModelAssembler;
import com.interview.demo.service.CategoryServiceContract;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryServiceContract categoryService;
  private final CategoryModelAssembler categoryAssembler;

  public CategoryController(
      CategoryServiceContract categoryService, CategoryModelAssembler categoryAssembler) {
    this.categoryService = categoryService;
    this.categoryAssembler = categoryAssembler;
  }

  @GetMapping
  public CollectionModel<EntityModel<CategoryResponse>> getAllCategories() {
    List<EntityModel<CategoryResponse>> categories =
        categoryService.getAllCategories().stream().map(categoryAssembler::toModel).toList();
    return CollectionModel.of(
        categories,
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CategoryController.class).getAllCategories())
            .withSelfRel());
  }

  @GetMapping("/{id}")
  public EntityModel<CategoryResponse> getCategoryById(@PathVariable Long id) {
    return categoryAssembler.toModel(categoryService.getCategoryById(id));
  }

  @GetMapping("/details")
  public CollectionModel<EntityModel<CategoryResponseDetails>> getAllCategoriesDetails(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
    List<EntityModel<CategoryResponseDetails>> categories =
        categoryService.getAllCategoriesDetails(page, size).stream()
            .map(categoryAssembler::toDetailsModel)
            .toList();
    return CollectionModel.of(
        categories,
        WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(CategoryController.class)
                    .getAllCategoriesDetails(page, size))
            .withSelfRel());
  }

  @PostMapping
  public EntityModel<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    return categoryAssembler.toModel(categoryService.createCategory(request));
  }

  @PutMapping("/{id}")
  public EntityModel<CategoryResponse> updateCategory(
      @PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) {
    return categoryAssembler.toModel(categoryService.updateCategory(id, request));
  }

  @DeleteMapping("/{id}")
  public Map<String, String> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return Map.of("message", "Category deleted successfully");
  }
}
