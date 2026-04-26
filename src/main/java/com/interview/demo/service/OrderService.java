package com.interview.demo.service;

import com.interview.demo.dto.order.CreateOrderItemRequest;
import com.interview.demo.dto.order.CreateOrderRequest;
import com.interview.demo.dto.order.OrderItemResponse;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.dto.order.UpdateOrderStatusRequest;
import com.interview.demo.entity.Order;
import com.interview.demo.entity.OrderItem;
import com.interview.demo.entity.OrderStatus;
import com.interview.demo.entity.Product;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.exception.ValidationException;
import com.interview.demo.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService implements OrderServiceContract {
  private final OrderRepository orderRepository;
  private final ProductServiceContract productService;

  public OrderService(OrderRepository orderRepository, ProductServiceContract productService) {
    this.orderRepository = orderRepository;
    this.productService = productService;
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getAllOrders() {
    return orderRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderById(Long orderId) {
    return toResponse(getById(orderId));
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
      Product product = productService.getById(itemRequest.productId());
      if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
        throw new ValidationException("Quantity must be greater than 0");
      }

      BigDecimal lineTotal =
          product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
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

  @Transactional(readOnly = true)
  public List<OrderItemResponse> getOrderItemsByOrderId(Long orderId) {
    Order order =
        orderRepository
            .findByIdWithItemsAndProducts(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    return order.getItems().stream().map(this::toItemResponse).toList();
  }

  @Transactional
  public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
    Order order = getById(orderId);
    order.setStatus(request.status());
    return toResponse(orderRepository.save(order));
  }

  @Transactional
  public void deleteOrder(Long orderId) {
    Order order = getById(orderId);
    orderRepository.delete(order);
  }

  private OrderResponse toResponse(Order order) {
    List<OrderItemResponse> items =
        order.getItems().stream().map(this::toItemResponse).toList();
    return new OrderResponse(
        order.getId(),
        order.getCustomerName(),
        order.getStatus(),
        order.getTotalAmount(),
        order.getCreatedAt(),
        items);
  }

  private OrderItemResponse toItemResponse(OrderItem item) {
    return new OrderItemResponse(
        item.getId(),
        item.getProduct().getId(),
        item.getProduct().getName(),
        item.getQuantity(),
        item.getUnitPrice(),
        item.getLineTotal());
  }

  private Order getById(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
  }
}
