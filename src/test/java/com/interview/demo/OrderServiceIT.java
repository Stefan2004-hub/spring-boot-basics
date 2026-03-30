package com.interview.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interview.demo.dto.order.CreateOrderItemRequest;
import com.interview.demo.dto.order.CreateOrderRequest;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.entity.Product;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.repository.OrderRepository;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.service.OrderServiceContract;
import com.interview.demo.support.PostgresContainerTestBase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class OrderServiceIT extends PostgresContainerTestBase {
  @Autowired private OrderServiceContract orderService;
  @Autowired private OrderRepository orderRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void clean() {
    orderRepository.deleteAll();
    productRepository.deleteAll();
  }

  @Test
  void shouldCreateOrderWithMultipleItemsInSingleTransaction() {
    Product laptop = productRepository.save(product("Laptop", new BigDecimal("1500.00")));
    Product mouse = productRepository.save(product("Mouse", new BigDecimal("50.00")));

    CreateOrderRequest request =
        new CreateOrderRequest(
            "Alice",
            List.of(new CreateOrderItemRequest(laptop.getId(), 2), new CreateOrderItemRequest(mouse.getId(), 3)));

    OrderResponse response = orderService.createOrder(request);

    assertThat(response.id()).isNotNull();
    assertThat(response.items()).hasSize(2);
    assertThat(response.totalAmount()).isEqualByComparingTo("3150.00");
    assertThat(response.createdAt()).isEqualTo(LocalDate.now());
    assertThat(orderRepository.count()).isEqualTo(1);
    assertThat(countOrderItems()).isEqualTo(2);
  }

  @Test
  void shouldRollbackOrderWhenOneItemIsInvalid() {
    Product laptop = productRepository.save(product("Laptop", new BigDecimal("1500.00")));
    long missingProductId = laptop.getId() + 9999L;

    CreateOrderRequest request =
        new CreateOrderRequest(
            "Bob",
            List.of(new CreateOrderItemRequest(laptop.getId(), 1), new CreateOrderItemRequest(missingProductId, 1)));

    assertThatThrownBy(() -> orderService.createOrder(request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Product not found");

    assertThat(orderRepository.count()).isZero();
    assertThat(countOrderItems()).isZero();
  }

  private Integer countOrderItems() {
    return jdbcTemplate.queryForObject("select count(*) from order_items", Integer.class);
  }

  private Product product(String name, BigDecimal price) {
    Product product = new Product();
    product.setName(name);
    product.setDescription(name + " description");
    product.setPrice(price);
    return product;
  }
}
