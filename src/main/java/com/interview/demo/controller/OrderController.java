package com.interview.demo.controller;

import com.interview.demo.dto.order.CreateOrderRequest;
import com.interview.demo.dto.order.OrderItemResponse;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.dto.order.UpdateOrderStatusRequest;
import com.interview.demo.hateoas.OrderModelAssembler;
import com.interview.demo.service.OrderServiceContract;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
  private final OrderModelAssembler orderAssembler;

  public OrderController(OrderServiceContract orderService, OrderModelAssembler orderAssembler) {
    this.orderService = orderService;
    this.orderAssembler = orderAssembler;
  }

  @GetMapping
  public CollectionModel<EntityModel<OrderResponse>> getAllOrders() {
    List<EntityModel<OrderResponse>> orders =
        orderService.getAllOrders().stream().map(orderAssembler::toModel).toList();
    return CollectionModel.of(
        orders,
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(OrderController.class).getAllOrders())
            .withSelfRel());
  }

  @GetMapping("/{id}")
  public EntityModel<OrderResponse> getOrderById(@PathVariable Long id) {
    return orderAssembler.toModel(orderService.getOrderById(id));
  }

  @PostMapping
  public EntityModel<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    return orderAssembler.toModel(orderService.createOrder(request));
  }

  @GetMapping("/{id}/items")
  public CollectionModel<EntityModel<OrderItemResponse>> getOrderItemsByOrderId(@PathVariable Long id) {
    List<EntityModel<OrderItemResponse>> items =
        orderService.getOrderItemsByOrderId(id).stream()
            .map(item -> orderAssembler.toItemModel(id, item))
            .toList();
    return CollectionModel.of(
        items,
        WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(OrderController.class).getOrderItemsByOrderId(id))
            .withSelfRel());
  }

  @PutMapping("/{id}")
  public EntityModel<OrderResponse> updateOrderStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
    return orderAssembler.toModel(orderService.updateOrderStatus(id, request));
  }

  @DeleteMapping("/{id}")
  public Map<String, String> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return Map.of("message", "Order deleted successfully");
  }
}
