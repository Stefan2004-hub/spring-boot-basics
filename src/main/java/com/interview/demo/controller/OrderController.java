package com.interview.demo.controller;

import com.interview.demo.dto.order.CreateOrderRequest;
import com.interview.demo.dto.order.OrderItemResponse;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.dto.order.UpdateOrderStatusRequest;
import com.interview.demo.hateoas.OrderModelAssembler;
import com.interview.demo.service.OrderServiceContract;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Operations related to order management")
public class OrderController {
  private final OrderServiceContract orderService;
  private final OrderModelAssembler orderAssembler;

  public OrderController(OrderServiceContract orderService, OrderModelAssembler orderAssembler) {
    this.orderService = orderService;
    this.orderAssembler = orderAssembler;
  }

  @GetMapping
  @Operation(summary = "Get all orders", description = "Retrieve a list of all orders")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved orders",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = CollectionModel.class)))
  public CollectionModel<EntityModel<OrderResponse>> getAllOrders() {
    List<EntityModel<OrderResponse>> orders =
        orderService.getAllOrders().stream().map(orderAssembler::toModel).toList();
    return CollectionModel.of(
        orders,
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(OrderController.class).getAllOrders())
            .withSelfRel());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved order",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = EntityModel.class)))
  @ApiResponse(responseCode = "404", description = "Order not found")
  public EntityModel<OrderResponse> getOrderById(@PathVariable Long id) {
    return orderAssembler.toModel(orderService.getOrderById(id));
  }

  @PostMapping
  @Operation(summary = "Create order", description = "Create a new order")
  @ApiResponse(responseCode = "201", description = "Order created successfully",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = EntityModel.class)))
  @ApiResponse(responseCode = "400", description = "Invalid order data")
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
  @Operation(summary = "Delete order", description = "Delete an order by ID")
  @ApiResponse(responseCode = "200", description = "Order deleted successfully")
  @ApiResponse(responseCode = "404", description = "Order not found")
  public Map<String, String> deleteOrder(@PathVariable Long id) {
    orderService.deleteOrder(id);
    return Map.of("message", "Order deleted successfully");
  }
}
