package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
}
