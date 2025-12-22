package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByCategories_Name(String name);
    List<Product> findByInventory_QtyLessThan(int limit);

}
