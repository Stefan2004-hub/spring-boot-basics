package com.interview.demo.repository.projection;

import java.util.List;

public interface CategoryProjection {
  Long getId();

  String getName();

  List<ProductSummary> getProducts();
}
