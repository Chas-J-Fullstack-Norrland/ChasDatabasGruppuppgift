package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = """
    UPDATE inventory
    SET quantity = quantity + :delta
    WHERE product_id = (
        SELECT id FROM product WHERE sku = :sku
    )
""", nativeQuery = true)
    int updateQuantityBySku(@Param("sku") String sku,
                            @Param("delta") int delta);
}
