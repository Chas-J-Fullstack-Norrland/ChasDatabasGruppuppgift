package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Category;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class InventoryRepositoryTest {

    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    TestEntityManager entityManager;

    List<Product> productList;

    @BeforeEach
    void setup(){

        Product productInStock = new Product("TestProduct","TST-PROD", BigDecimal.valueOf(81230.02));
        Product productNotInStock =  new Product("TestProduct2","TST-PROD2", BigDecimal.valueOf(810.02));

        productList = productRepository.saveAll(List.of(productInStock,productNotInStock));
        inventoryRepository.updateQuantityBySku("TST-PROD",10);
    }

    //No CREATE due to close cascading relationship with Product

    @Test
    void updateQuantityOnProductInInventory(){

        assertFalse(entityManager.find(Inventory.class,productList.getLast().getId()).getQty()>0);

        Optional<Inventory> inventoryUpdate = inventoryRepository.findById(productList.getLast().getId());
        assertTrue(inventoryUpdate.isPresent());

        inventoryUpdate.get().setQty(2);

        //No save since inventory is a managed entity here
        assertTrue(entityManager.find(Inventory.class,productList.getLast().getId()).getQty()==2);


    }

    @Test
    void readQuantityOnProductInInventoryByProductID(){
        Optional<Inventory> fetchedInventory = inventoryRepository.findById(productList.getFirst().getId());
        assertTrue(fetchedInventory.isPresent());

        assertEquals(10,fetchedInventory.get().getQty());
    }

    @Test
    void CannotDeleteInventoryEntryWithoutRemovingParentProduct(){

        inventoryRepository.deleteById(productList.getLast().getId());
        assertTrue(inventoryRepository.existsById(productList.getLast().getId()));
        productRepository.deleteById(productList.getLast().getId());
        assertFalse(inventoryRepository.existsById(productList.getLast().getId()));


    }

    @Test
    void ShouldAddQTYToStock(){

        inventoryRepository.updateQuantityBySku(productList.getLast().getSku(),5);
        assertEquals(5,entityManager.find(Inventory.class,productList.getLast().getId()).getQty());

    }




}