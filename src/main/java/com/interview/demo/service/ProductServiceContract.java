package com.interview.demo.service;

import com.interview.demo.dto.PatchProductRequest;
import com.interview.demo.dto.ProductDTO;
import com.interview.demo.dto.ProductResponse;
import com.interview.demo.entity.Product;
import com.interview.demo.repository.projection.ProductSummary;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductServiceContract {
  List<ProductResponse> getAllProducts();

  Optional<ProductResponse> getProductById(Long id);

  ProductResponse saveProduct(ProductDTO product);

  List<ProductResponse> findByCategoryName(String categoryName);

  Page<ProductResponse> searchProducts(
      String name, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

  List<ProductSummary> getProductSummaries();

  ProductResponse updateProduct(Long id, ProductDTO product);

  ProductResponse patchProduct(Long id, PatchProductRequest request);

  void deleteProduct(Long id);

  Product getById(Long productId);
}
