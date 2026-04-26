package com.interview.demo.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.interview.demo.controller.CategoryController;
import com.interview.demo.controller.ProductController;
import com.interview.demo.dto.ProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ProductModelAssembler
    implements RepresentationModelAssembler<ProductResponse, EntityModel<ProductResponse>> {

  @Override
  public EntityModel<ProductResponse> toModel(ProductResponse product) {
    EntityModel<ProductResponse> model =
        EntityModel.of(
            product,
            linkTo(methodOn(ProductController.class).getProductById(product.id())).withSelfRel(),
            linkTo(methodOn(ProductController.class).getAllProducts()).withRel("products"));

    if (product.categoryId() != null) {
      model.add(
          linkTo(methodOn(CategoryController.class).getCategoryById(product.categoryId()))
              .withRel("category"));
    }

    return model;
  }
}
