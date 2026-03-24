package com.interview.demo.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 150, message = "Customer name must be between 2 and 150 characters")
    String customerName,
    @NotEmpty(message = "Order must contain at least one item")
    List<@Valid CreateOrderItemRequest> items) {}
