package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    List<Reservation> findByCustomerEmail(String email);
    void deleteAllByCustomerId(Long id);
}
