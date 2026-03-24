package com.interview.demo.controller;

import com.interview.demo.dto.ProductDTO;
import com.interview.demo.dto.ProductResponse;
import com.interview.demo.repository.projection.ProductSummary;
import com.interview.demo.service.ProductService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {
  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public List<ProductResponse> getAllProducts() {
    return productService.getAllProducts();
  }

  @GetMapping("/by-category/{categoryName}")
  public List<ProductResponse> findByCategoryName(@PathVariable String categoryName) {
    return productService.findByCategoryName(categoryName);
  }

  @GetMapping("/search")
  public List<ProductResponse> searchProducts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice) {
    return productService.searchProducts(name, minPrice, maxPrice);
  }

  @GetMapping("/summaries")
  public List<ProductSummary> getProductSummaries() {
    return productService.getProductSummaries();
  }

  @PostMapping
  public ProductResponse createProduct(@Valid @RequestBody ProductDTO product) {
    return productService.saveProduct(product);
  }
}
