package com.interview.demo.service;

import com.interview.demo.dto.order.CreateOrderItemRequest;
import com.interview.demo.dto.order.CreateOrderRequest;
import com.interview.demo.dto.order.OrderItemResponse;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.entity.Order;
import com.interview.demo.entity.OrderItem;
import com.interview.demo.entity.OrderStatus;
import com.interview.demo.entity.Product;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.exception.ValidationException;
import com.interview.demo.repository.OrderRepository;
import com.interview.demo.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
  }

  @Transactional
  public OrderResponse createOrder(CreateOrderRequest request) {
    if (request.items() == null || request.items().isEmpty()) {
      throw new ValidationException("Order must contain at least one item");
    }
    if (request.customerName() == null || request.customerName().isBlank()) {
      throw new ValidationException("Customer name is required");
    }

    Order order = new Order();
    order.setCustomerName(request.customerName().trim());
    order.setStatus(OrderStatus.CREATED);
    order.setCreatedAt(LocalDate.now());

    BigDecimal totalAmount = BigDecimal.ZERO;
    for (CreateOrderItemRequest itemRequest : request.items()) {
      Product product =
          productRepository
              .findById(itemRequest.productId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Product not found: " + itemRequest.productId()));
      if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
        throw new ValidationException("Quantity must be greater than 0");
      }

      BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
      OrderItem item = new OrderItem();
      item.setProduct(product);
      item.setQuantity(itemRequest.quantity());
      item.setUnitPrice(product.getPrice());
      item.setLineTotal(lineTotal);
      order.addItem(item);
      totalAmount = totalAmount.add(lineTotal);
    }
    order.setTotalAmount(totalAmount);

    // Atomicity: either both parent order and all child order_items are committed, or all rolled
    // back on error.
    Order savedOrder = orderRepository.save(order);
    return toResponse(savedOrder);
  }

  private OrderResponse toResponse(Order order) {
    List<OrderItemResponse> items =
        order.getItems().stream()
            .map(
                item ->
                    new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()))
            .toList();
    return new OrderResponse(
        order.getId(), order.getCustomerName(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt(), items);
  }
}
