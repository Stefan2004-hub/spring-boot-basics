package com.interview.demo.service;

import com.interview.demo.dto.ProductDTO;
import com.interview.demo.dto.ProductResponse;
import com.interview.demo.entity.Category;
import com.interview.demo.entity.Product;
import com.interview.demo.exception.ConflictException;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.repository.projection.ProductSummary;
import com.interview.demo.repository.specification.ProductSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final CategoryService categoryService;

  public ProductService(ProductRepository productRepository, CategoryService categoryService) {
    this.productRepository = productRepository;
    this.categoryService = categoryService;
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
      Category category = categoryService.getById(product.categoryId());
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

  @Transactional
  public ProductResponse updateProduct(Long id, ProductDTO product) {
    Product existing = getById(id);
    existing.setName(product.name());
    existing.setDescription(product.description());
    existing.setPrice(product.price());
    if (product.categoryId() != null) {
      Category category = categoryService.getById(product.categoryId());
      existing.setCategory(category);
    } else {
      existing.setCategory(null);
    }
    return toResponse(productRepository.save(existing));
  }

  @Transactional
  public void deleteProduct(Long id) {
    Product product = getById(id);
    try {
      productRepository.delete(product);
      productRepository.flush();
    } catch (DataIntegrityViolationException ex) {
      throw new ConflictException("Product is in use and cannot be deleted: " + id);
    }
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

  public Product getById(Long productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
  }
}
