package com.interview.demo.dto;

import com.interview.demo.repository.projection.ProductSummary;
import java.util.List;

public record CategoryResponseDetails(Long id, String name, List<ProductSummary> products) {}
