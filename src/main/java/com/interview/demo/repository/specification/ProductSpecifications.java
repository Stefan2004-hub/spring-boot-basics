package com.interview.demo.repository.specification;

import com.interview.demo.entity.Product;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {
  private ProductSpecifications() {}

  public static Specification<Product> nameContains(String name) {
    return (root, query, criteriaBuilder) ->
        name == null || name.isBlank()
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
  }

  public static Specification<Product> priceGte(BigDecimal minPrice) {
    return (root, query, criteriaBuilder) ->
        minPrice == null ? criteriaBuilder.conjunction() : criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
  }

  public static Specification<Product> priceLte(BigDecimal maxPrice) {
    return (root, query, criteriaBuilder) ->
        maxPrice == null ? criteriaBuilder.conjunction() : criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
  }
}
