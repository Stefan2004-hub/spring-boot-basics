package com.interview.demo.service;

import com.interview.demo.dto.ProductDTO;
import com.interview.demo.dto.PatchProductRequest;
import com.interview.demo.dto.ProductResponse;
import com.interview.demo.entity.Category;
import com.interview.demo.entity.Product;
import com.interview.demo.exception.ConflictException;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.exception.ValidationException;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.repository.projection.ProductSummary;
import com.interview.demo.repository.specification.ProductSpecifications;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  public Page<ProductResponse> searchProducts(
      String name, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
    if (page < 1) {
      throw new ValidationException("page must be at least 1");
    }
    if (size < 1 || size > 100) {
      throw new ValidationException("size must be between 1 and 100");
    }

    Specification<Product> spec =
        ProductSpecifications.nameContains(name)
            .and(ProductSpecifications.priceGte(minPrice))
            .and(ProductSpecifications.priceLte(maxPrice));
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
    return productRepository.findAll(spec, pageable).map(this::toResponse);
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
  public ProductResponse patchProduct(Long id, PatchProductRequest request) {
    Product existing = getById(id);
    validatePatchRequest(request);

    if (request.hasName()) {
      String name = request.getName();
      if (name == null || name.isBlank()) {
        throw new ValidationException("Product name is required");
      }
      if (name.length() < 2 || name.length() > 150) {
        throw new ValidationException("Product name must be between 2 and 150 characters");
      }
      existing.setName(name);
    }

    if (request.hasDescription()) {
      String description = request.getDescription();
      if (description == null || description.isBlank()) {
        throw new ValidationException("Product description is required");
      }
      if (description.length() < 5 || description.length() > 1000) {
        throw new ValidationException("Product description must be between 5 and 1000 characters");
      }
      existing.setDescription(description);
    }

    if (request.hasPrice()) {
      BigDecimal price = request.getPrice();
      if (price == null) {
        throw new ValidationException("Product price is required");
      }
      if (price.compareTo(new BigDecimal("0.01")) < 0) {
        throw new ValidationException("Product price must be greater than 0");
      }
      if (price.scale() > 2) {
        throw new ValidationException("Product price must have up to 2 decimals");
      }
      if (price.precision() - price.scale() > 17) {
        throw new ValidationException("Product price must have up to 2 decimals");
      }
      existing.setPrice(price);
    }

    if (request.hasCategoryId()) {
      Long categoryId = request.getCategoryId();
      if (categoryId == null) {
        throw new ValidationException("Category id cannot be null");
      }
      if (categoryId <= 0) {
        throw new ValidationException("Category id must be positive");
      }
      Category category = categoryService.getById(categoryId);
      existing.setCategory(category);
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

  private void validatePatchRequest(PatchProductRequest request) {
    if (request == null) {
      throw new ValidationException("Patch payload is required");
    }
    if (!request.getUnknownFields().isEmpty()) {
      String unknownField = request.getUnknownFields().iterator().next();
      throw new ValidationException("Unknown field: " + unknownField);
    }
    if (request.hasNoUpdatableFields()) {
      throw new ValidationException("At least one field must be provided");
    }
  }
}
