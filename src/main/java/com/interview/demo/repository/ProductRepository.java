package com.interview.demo.repository;

import com.interview.demo.entity.Product;
import com.interview.demo.repository.projection.ProductSummary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  @Query("select p from Product p join p.category c where lower(c.name) = lower(:categoryName)")
  List<Product> findByCategoryName(@Param("categoryName") String categoryName);

  @Query(
      """
      select p
      from Product p
      join fetch p.category c
      where c.id in :categoryIds
      order by c.id asc, p.name asc, p.id asc
      """)
  List<Product> findByCategoryIdsWithCategoryOrdered(@Param("categoryIds") List<Long> categoryIds);

  List<ProductSummary> findAllProjectedBy();
}
