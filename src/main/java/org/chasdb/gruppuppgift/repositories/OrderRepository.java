package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    @Query(value = """
       SELECT 
           CAST(order_date AS DATE), 
           SUM(total_price)
       FROM orders
       WHERE status = 'PAID' 
       AND order_date BETWEEN :startDate AND :endDate
       GROUP BY CAST(order_date AS DATE)
       ORDER BY CAST(order_date AS DATE) DESC
       """, nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}
