package org.chasdb.gruppuppgift.repositories;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct(Product product);

    List<Inventory> findByQtyLessThan(int limit);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.sku = :sku")
    Optional<Inventory> findByProductSkuWithLock(@Param("sku") String sku);

    Optional<Inventory> findByProduct_Sku(String sku);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Inventory i SET i.qty = i.qty + :delta WHERE i.product.sku = :sku")
    int updateQuantityBySku(@Param("sku") String sku,
                            @Param("delta") int delta);
}
