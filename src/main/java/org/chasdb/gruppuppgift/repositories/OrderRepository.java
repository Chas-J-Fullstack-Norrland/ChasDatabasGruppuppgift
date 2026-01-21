package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    @Query(value = """
       SELECT 
           CAST(created_at AS DATE), 
           SUM(total_price)
       FROM orders
       WHERE status = 'PAID' 
       AND created_at BETWEEN :startDate AND :endDate
       GROUP BY CAST(created_at AS DATE)
       ORDER BY CAST(created_at AS DATE) DESC
       """, nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);


    Optional<Order> findByCode(String code);

}

