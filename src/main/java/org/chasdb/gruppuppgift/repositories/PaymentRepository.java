package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {

    boolean existsByOrderIdAndStatus(Long orderId, String status);
    List<Payment> findAllByOrderId(Long orderID);

}
