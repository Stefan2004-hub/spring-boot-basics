package com.interview.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductDTO(
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
    String name,
    @NotBlank(message = "Product description is required")
    @Size(min = 5, max = 1000, message = "Product description must be between 5 and 1000 characters")
    String description,
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Product price must have up to 2 decimals")
    BigDecimal price,
    @Positive(message = "Category id must be positive")
    Long categoryId) {}
