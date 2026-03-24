package com.interview.demo.service;

import com.interview.demo.dto.ProductDTO;
import com.interview.demo.dto.ProductResponse;
import com.interview.demo.entity.Category;
import com.interview.demo.entity.Product;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.repository.CategoryRepository;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.repository.projection.ProductSummary;
import com.interview.demo.repository.specification.ProductSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  public ProductService(
      ProductRepository productRepository, CategoryRepository categoryRepository) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> getAllProducts() {
    return productRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public Optional<ProductResponse> getProductById(Long id) {
    return productRepository.findById(id).map(this::toResponse);
  }

  @Transactional
  public ProductResponse saveProduct(ProductDTO product) {
    Product newProduct = new Product();
    newProduct.setName(product.name());
    newProduct.setDescription(product.description());
    newProduct.setPrice(product.price());
    if (product.categoryId() != null) {
      Category category =
          categoryRepository
              .findById(product.categoryId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Category not found: " + product.categoryId()));
      newProduct.setCategory(category);
    }
    return toResponse(productRepository.save(newProduct));
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> findByCategoryName(String categoryName) {
    return productRepository.findByCategoryName(categoryName).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> searchProducts(
      String name, BigDecimal minPrice, BigDecimal maxPrice) {
    Specification<Product> spec =
        ProductSpecifications.nameContains(name)
            .and(ProductSpecifications.priceGte(minPrice))
            .and(ProductSpecifications.priceLte(maxPrice));
    return productRepository.findAll(spec).stream().map(this::toResponse).toList();
  }

  public List<ProductSummary> getProductSummaries() {
    return productRepository.findAllProjectedBy();
  }

  public void deleteProduct(Long id) {
    productRepository.deleteById(id);
  }

  private ProductResponse toResponse(Product product) {
    Category category = product.getCategory();
    Long categoryId = category != null ? category.getId() : null;
    String categoryName = category != null ? category.getName() : null;
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        categoryId,
        categoryName);
  }
}
