package com.interview.demo.dto;

import java.math.BigDecimal;

public record ProductDTO(String name, String description, BigDecimal price, Long categoryId) {}
