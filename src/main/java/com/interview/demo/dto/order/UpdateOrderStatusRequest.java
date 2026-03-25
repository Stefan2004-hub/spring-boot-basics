package com.interview.demo.dto.order;

import com.interview.demo.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Order status is required") OrderStatus status) {}
