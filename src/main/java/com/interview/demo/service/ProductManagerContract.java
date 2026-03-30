package com.interview.demo.service;

import com.interview.demo.dto.CategoryProductSummaryResponse;
import java.util.List;
import java.util.Map;

public interface ProductManagerContract {
  Map<Long, List<CategoryProductSummaryResponse>> getProductSummaries(List<Long> categoryIds);
}
