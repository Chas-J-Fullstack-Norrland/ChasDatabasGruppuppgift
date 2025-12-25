package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.chasdb.gruppuppgift.models.dto.TopProductDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
       SELECT oi.product, SUM(oi.quantity)
       FROM OrderItem oi
       GROUP BY oi.product
       ORDER BY SUM(oi.quantity) DESC
       """)
    List<Object[]> findTopSellingProductsRaw(Pageable pageable);
}
