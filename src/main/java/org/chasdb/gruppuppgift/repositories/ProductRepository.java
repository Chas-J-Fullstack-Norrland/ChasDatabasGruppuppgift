package org.chasdb.gruppuppgift.repositories;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByCategories_Name(String name);
    List<Product> findByInventory_QtyLessThan(int limit);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Product p SET p.active = true WHERE p.id = :id")
    void enableProduct(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Product p SET p.active = false WHERE p.id = :id")
    void disableProduct(@Param("id") Long id);

}
