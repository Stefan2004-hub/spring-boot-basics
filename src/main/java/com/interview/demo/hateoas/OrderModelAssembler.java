package com.interview.demo.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.interview.demo.controller.OrderController;
import com.interview.demo.controller.ProductController;
import com.interview.demo.dto.order.OrderItemResponse;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.dto.order.UpdateOrderStatusRequest;
import com.interview.demo.entity.OrderStatus;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class OrderModelAssembler
    implements RepresentationModelAssembler<OrderResponse, EntityModel<OrderResponse>> {

  @Override
  public EntityModel<OrderResponse> toModel(OrderResponse order) {
    EntityModel<OrderResponse> model =
        EntityModel.of(
            order,
            linkTo(methodOn(OrderController.class).getOrderById(order.id())).withSelfRel(),
            linkTo(methodOn(OrderController.class).getAllOrders()).withRel("orders"),
            linkTo(methodOn(OrderController.class).getOrderItemsByOrderId(order.id()))
                .withRel("items"));

    if (order.status() == OrderStatus.CREATED) {
      model.add(
          linkTo(
                  methodOn(OrderController.class)
                      .updateOrderStatus(
                          order.id(), new UpdateOrderStatusRequest(OrderStatus.CONFIRMED)))
              .withRel("confirm"));
      model.add(
          linkTo(
                  methodOn(OrderController.class)
                      .updateOrderStatus(
                          order.id(), new UpdateOrderStatusRequest(OrderStatus.CANCELLED)))
              .withRel("cancel"));
    }

    return model;
  }

  public EntityModel<OrderItemResponse> toItemModel(Long orderId, OrderItemResponse item) {
    return EntityModel.of(
        item,
        linkTo(methodOn(ProductController.class).getProductById(item.productId())).withRel("product"),
        linkTo(methodOn(OrderController.class).getOrderById(orderId)).withRel("order"));
  }
}
