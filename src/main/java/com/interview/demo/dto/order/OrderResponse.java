package com.interview.demo.dto.order;

import com.interview.demo.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
    Long id,
    String customerName,
    OrderStatus status,
    BigDecimal totalAmount,
    LocalDate createdAt,
    List<OrderItemResponse> items) {}
