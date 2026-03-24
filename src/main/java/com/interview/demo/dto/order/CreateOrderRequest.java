package com.interview.demo.dto.order;

import java.util.List;

public record CreateOrderRequest(String customerName, List<CreateOrderItemRequest> items) {}
