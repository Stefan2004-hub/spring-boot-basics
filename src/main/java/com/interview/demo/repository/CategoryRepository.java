package com.interview.demo.repository;

import com.interview.demo.entity.Category;
import com.interview.demo.repository.projection.CategoryProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByNameIgnoreCase(String name);

  @Query("SELECT distinct c FROM Category c LEFT JOIN FETCH c.products")
  List<CategoryProjection> findAllWithProductDetails();
}
