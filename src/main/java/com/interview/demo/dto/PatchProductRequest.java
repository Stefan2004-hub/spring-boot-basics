package com.interview.demo.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchProductRequest {
  private String name;
  private String description;
  private BigDecimal price;
  private Long categoryId;

  private boolean nameSet;
  private boolean descriptionSet;
  private boolean priceSet;
  private boolean categoryIdSet;

  private final Set<String> unknownFields = new LinkedHashSet<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    this.nameSet = true;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
    this.descriptionSet = true;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
    this.priceSet = true;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
    this.categoryIdSet = true;
  }

  @JsonAnySetter
  public void captureUnknownField(String key, Object value) {
    unknownFields.add(key);
  }

  public boolean hasName() {
    return nameSet;
  }

  public boolean hasDescription() {
    return descriptionSet;
  }

  public boolean hasPrice() {
    return priceSet;
  }

  public boolean hasCategoryId() {
    return categoryIdSet;
  }

  public boolean hasNoUpdatableFields() {
    return !nameSet && !descriptionSet && !priceSet && !categoryIdSet;
  }

  @JsonIgnore
  public Set<String> getUnknownFields() {
    return unknownFields;
  }
}
