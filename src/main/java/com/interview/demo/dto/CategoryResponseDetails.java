package com.interview.demo.dto;

import java.util.List;

public record CategoryResponseDetails(Long id, String name, List<CategoryProductSummaryResponse> products) {}
