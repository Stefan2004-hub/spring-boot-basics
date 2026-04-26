package com.interview.demo.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.interview.demo.controller.CategoryController;
import com.interview.demo.controller.ProductController;
import com.interview.demo.dto.CategoryResponse;
import com.interview.demo.dto.CategoryResponseDetails;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CategoryModelAssembler
    implements RepresentationModelAssembler<CategoryResponse, EntityModel<CategoryResponse>> {

  @Override
  public EntityModel<CategoryResponse> toModel(CategoryResponse category) {
    return EntityModel.of(
        category,
        linkTo(methodOn(CategoryController.class).getCategoryById(category.id())).withSelfRel(),
        linkTo(methodOn(CategoryController.class).getAllCategories()).withRel("categories"),
        linkTo(methodOn(ProductController.class).findByCategoryName(category.name()))
            .withRel("products"));
  }

  public EntityModel<CategoryResponseDetails> toDetailsModel(CategoryResponseDetails category) {
    return EntityModel.of(
        category,
        linkTo(methodOn(CategoryController.class).getCategoryById(category.id())).withSelfRel(),
        linkTo(methodOn(CategoryController.class).getAllCategories()).withRel("categories"),
        linkTo(methodOn(ProductController.class).findByCategoryName(category.name()))
            .withRel("products"));
  }
}
