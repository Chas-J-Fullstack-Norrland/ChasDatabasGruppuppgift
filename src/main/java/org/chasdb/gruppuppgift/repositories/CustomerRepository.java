package org.chasdb.gruppuppgift.repositories;


import org.chasdb.gruppuppgift.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
