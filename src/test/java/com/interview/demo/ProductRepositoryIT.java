package com.interview.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.interview.demo.entity.Category;
import com.interview.demo.entity.Product;
import com.interview.demo.repository.CategoryRepository;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.repository.projection.ProductSummary;
import com.interview.demo.repository.specification.ProductSpecifications;
import com.interview.demo.support.PostgresContainerTestBase;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

@SpringBootTest
class ProductRepositoryIT extends PostgresContainerTestBase {
  @Autowired private ProductRepository productRepository;
  @Autowired private CategoryRepository categoryRepository;

  @BeforeEach
  void clean() {
    productRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  @Test
  void shouldFindProductsByCategoryNameUsingJoinQuery() {
    Category electronics = categoryRepository.save(new Category(null, "Electronics"));
    Category books = categoryRepository.save(new Category(null, "Books"));
    productRepository.save(product("Laptop", "Work laptop", new BigDecimal("1499.99"), electronics));
    productRepository.save(product("Mouse", "Wireless mouse", new BigDecimal("29.99"), electronics));
    productRepository.save(product("Novel", "Book", new BigDecimal("19.99"), books));

    List<Product> products = productRepository.findByCategoryName("electronics");

    assertThat(products).hasSize(2);
    assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Laptop", "Mouse");
  }

  @Test
  void shouldFilterProductsWithCombinedSpecifications() {
    productRepository.save(product("Gaming Laptop", "A", new BigDecimal("1600.00"), null));
    productRepository.save(product("Office Laptop", "B", new BigDecimal("900.00"), null));
    productRepository.save(product("Gaming Mouse", "C", new BigDecimal("80.00"), null));

    Specification<Product> spec =
        Specification.where(ProductSpecifications.nameContains("laptop"))
            .and(ProductSpecifications.priceGte(new BigDecimal("1000.00")))
            .and(ProductSpecifications.priceLte(new BigDecimal("1700.00")));

    List<Product> results = productRepository.findAll(spec);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getName()).isEqualTo("Gaming Laptop");
  }

  @Test
  void shouldReturnProductSummariesProjection() {
    productRepository.save(product("Monitor", "27 inch", new BigDecimal("299.99"), null));
    productRepository.save(product("Keyboard", "Mechanical", new BigDecimal("89.99"), null));

    List<ProductSummary> summaries = productRepository.findAllProjectedBy();

    assertThat(summaries).extracting(ProductSummary::getName).contains("Monitor", "Keyboard");
    assertThat(summaries).extracting(ProductSummary::getPrice).contains(new BigDecimal("299.99"), new BigDecimal("89.99"));
  }

  private Product product(String name, String description, BigDecimal price, Category category) {
    Product product = new Product();
    product.setName(name);
    product.setDescription(description);
    product.setPrice(price);
    product.setCategory(category);
    return product;
  }
}
