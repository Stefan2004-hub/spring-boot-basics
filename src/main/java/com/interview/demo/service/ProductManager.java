package com.interview.demo.service;

import com.interview.demo.dto.CategoryProductSummaryResponse;
import com.interview.demo.entity.Product;
import com.interview.demo.repository.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProductManager {
  private final ProductRepository productRepository;

  public ProductManager(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Map<Long, List<CategoryProductSummaryResponse>> getProductSummaries(
      List<Long> categoryIds) {
    return productRepository.findByCategoryIdsWithCategoryOrdered(categoryIds).stream()
        .collect(
            Collectors.groupingBy(
                product -> product.getCategory().getId(),
                Collectors.mapping(this::toCategoryProductSummaryResponse, Collectors.toList())));
  }

  private CategoryProductSummaryResponse toCategoryProductSummaryResponse(Product product) {
    return new CategoryProductSummaryResponse(product.getName(), product.getPrice());
  }
}
