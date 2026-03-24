package com.interview.demo.repository;

import com.interview.demo.entity.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @Query(
      """
      select distinct o
      from Order o
      left join fetch o.items i
      left join fetch i.product
      where o.id = :id
      """)
  Optional<Order> findByIdWithItemsAndProducts(Long id);
}
