package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
