package com.interview.demo.service;

import com.interview.demo.dto.order.CreateOrderRequest;
import com.interview.demo.dto.order.OrderItemResponse;
import com.interview.demo.dto.order.OrderResponse;
import com.interview.demo.dto.order.UpdateOrderStatusRequest;
import java.util.List;

public interface OrderServiceContract {
  OrderResponse createOrder(CreateOrderRequest request);

  List<OrderItemResponse> getOrderItemsByOrderId(Long orderId);

  OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

  void deleteOrder(Long orderId);
}
