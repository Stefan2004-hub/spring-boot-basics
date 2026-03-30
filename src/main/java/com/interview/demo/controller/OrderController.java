package com.interview.demo.controller;

import com.interview.demo.dto.order.CreateOrderRequest;
import com.interview.demo.dto.order.OrderItemResponse;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.dto.order.UpdateOrderStatusRequest;
import com.interview.demo.service.OrderServiceContract;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderServiceContract orderService;

  public OrderController(OrderServiceContract orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
    return orderService.createOrder(request);
  }

  @GetMapping("/{id}/items")
  public List<OrderItemResponse> getOrderItemsByOrderId(@PathVariable Long id) {
    return orderService.getOrderItemsByOrderId(id);
  }

  @PutMapping("/{id}")
  public OrderResponse updateOrderStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
    return orderService.updateOrderStatus(id, request);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return Map.of("message", "Order deleted successfully");
  }
}
