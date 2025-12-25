package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Product;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategories_Name(String name);

    List<Product> findByInventory_QuantityLessThan(int limit);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.active = true WHERE p.id = :id")
    void enableProduct(@Param("id") Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.active = false WHERE p.id = :id")
    void disableProduct(@Param("id") Long id);

}
