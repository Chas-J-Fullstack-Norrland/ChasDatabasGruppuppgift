package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    //Top Sellers
    @Query("""
            SELECT oi.product, SUM(oi.qty)
            FROM OrderItem oi
            GROUP BY oi.product
            ORDER BY SUM(oi.qty) DESC
            """)
    List<Object[]> findTopSellingProducts(Pageable pageable);
    //Revenue per day
    @Query("""
            SELECT o.createdAt, SUM(oi.rowTotal)
            FROM OrderItem oi
            JOIN oi.order o
            GROUP BY o.createdAt
            ORDER BY o.createdAt
            """)
    List<Object[]> findRevenuePerDay();
}
