package org.chasdb.gruppuppgift.services;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductServiceTest {

    @Autowired
    ProductService productService;
    @Autowired
    ProductRepository repo;
    @Autowired
    InventoryRepository inventoryRepository;

    Product testProduct1;
    Product testProduct2;
    Product testProduct3;
    Product testProduct4;

    @AfterEach
    void cleanup(){
        repo.deleteAll();
    }

    @BeforeEach
    void setup(){

        repo.deleteAll();
        testProduct1 = productService.addProduct("Product 1","Prod1", BigDecimal.valueOf(29.22));
        testProduct2 = productService.addProduct("Product 2","Prod2", BigDecimal.valueOf(39.22));
        testProduct3 = productService.addProduct("Product 3","Prod3", BigDecimal.valueOf(49.22));
        testProduct4 = productService.addProduct("Product 4","Prod4", BigDecimal.valueOf(59.22));


    }


    @Test
    @Transactional
    void ShouldNotaddProductWithDuplicateOrBlankSKU() {



        Exception IllegalStateE = assertThrows(IllegalStateException.class,()->
                productService.addProduct("Product 1","Prod1", BigDecimal.valueOf(29.22)));


        Exception IllegalArgument = assertThrows(IllegalArgumentException.class,()->
                productService.addProduct("","Prod6", BigDecimal.valueOf(29.22)));

    }

    @Test
    @Transactional
    void saveProductShouldThrowUponDuplicatesUnderDifferentID() {

        Product testProduct = new Product("DuplicateSKU product", testProduct1.getSku(),BigDecimal.valueOf(999) );
        Exception IllegalState = assertThrows(IllegalStateException.class,()->
                productService.saveProduct(testProduct));

    }

    @Test
    @Transactional
    void listProductsWithInventory_QtyLessThan() {

        productService.addStockToProduct(testProduct1.getSku(),11);

        assertEquals(3, productService.listProductsWithInventory_QtyLessThan(10).size());


    }

    @Test
    @Transactional
    void addStockToProduct() {

        productService.addStockToProduct(testProduct2.getSku(), 22);
        assertEquals(22,inventoryRepository.findById(testProduct2.getId()).get().getQty());

    }
}