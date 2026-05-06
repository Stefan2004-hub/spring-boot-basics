package com.interview.demo.controller;

import com.interview.demo.dto.ProductDTO;
import com.interview.demo.dto.ProductResponse;
import com.interview.demo.dto.ProductSummaryResponse;
import com.interview.demo.dto.PatchProductRequest;
import com.interview.demo.hateoas.ProductModelAssembler;
import com.interview.demo.service.ProductServiceContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Operations related to product management")
public class ProductController {
  private final ProductServiceContract productService;
  private final ProductModelAssembler productAssembler;

  public ProductController(
      ProductServiceContract productService, ProductModelAssembler productAssembler) {
    this.productService = productService;
    this.productAssembler = productAssembler;
  }

  @GetMapping
  @Operation(summary = "Get all products", description = "Retrieve a list of all available products")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = CollectionModel.class)))
  public CollectionModel<EntityModel<ProductResponse>> getAllProducts() {
    List<EntityModel<ProductResponse>> products =
        productService.getAllProducts().stream().map(productAssembler::toModel).toList();
    return CollectionModel.of(
        products,
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class).getAllProducts())
            .withSelfRel());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved product",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = EntityModel.class)))
  @ApiResponse(responseCode = "404", description = "Product not found")
  public EntityModel<ProductResponse> getProductById(@PathVariable Long id) {
    return productAssembler.toModel(productService.getProductById(id));
  }

  @GetMapping("/by-category/{categoryName}")
  public CollectionModel<EntityModel<ProductResponse>> findByCategoryName(
      @PathVariable String categoryName) {
    List<EntityModel<ProductResponse>> products =
        productService.findByCategoryName(categoryName).stream()
            .map(productAssembler::toModel)
            .toList();
    return CollectionModel.of(
        products,
        WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ProductController.class).findByCategoryName(categoryName))
            .withSelfRel());
  }

  @GetMapping("/search")
  public PagedModel<EntityModel<ProductResponse>> searchProducts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      PagedResourcesAssembler<ProductResponse> pagedResourcesAssembler) {
    Page<ProductResponse> result = productService.searchProducts(name, minPrice, maxPrice, page, size);
    return pagedResourcesAssembler.toModel(result, productAssembler);
  }

  @GetMapping("/summaries")
  public CollectionModel<ProductSummaryResponse> getProductSummaries() {
    return CollectionModel.of(
        productService.getProductSummaries(),
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class).getProductSummaries())
            .withSelfRel());
  }

  @PostMapping
  @Operation(summary = "Create product", description = "Create a new product")
  @ApiResponse(responseCode = "201", description = "Product created successfully",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = EntityModel.class)))
  @ApiResponse(responseCode = "400", description = "Invalid product data")
  public EntityModel<ProductResponse> createProduct(@Valid @RequestBody ProductDTO product) {
    return productAssembler.toModel(productService.saveProduct(product));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update product", description = "Update an existing product by ID")
  @ApiResponse(responseCode = "200", description = "Product updated successfully",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = EntityModel.class)))
  @ApiResponse(responseCode = "400", description = "Invalid product data")
  @ApiResponse(responseCode = "404", description = "Product not found")
  public EntityModel<ProductResponse> updateProduct(
      @PathVariable Long id, @Valid @RequestBody ProductDTO product) {
    return productAssembler.toModel(productService.updateProduct(id, product));
  }

  @PatchMapping("/{id}")
  public EntityModel<ProductResponse> patchProduct(
      @PathVariable Long id, @RequestBody PatchProductRequest request) {
    return productAssembler.toModel(productService.patchProduct(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete product", description = "Delete a product by ID")
  @ApiResponse(responseCode = "200", description = "Product deleted successfully")
  @ApiResponse(responseCode = "404", description = "Product not found")
  public Map<String, String> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return Map.of("message", "Product deleted successfully");
  }
}
