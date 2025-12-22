package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Category;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void setup(){
        Category cat1 = new Category("TestCategory1");
        Category cat2 = new Category("CategoryTest2");
        categoryRepository.saveAll(List.of(cat1,cat2));
        Product newProduct = new Product("TestProduct","TST-PROD", BigDecimal.valueOf(81230.02));
        newProduct.addCategory(cat1);
        Product DBproduct = productRepository.save(newProduct);
    }

    @Test
    void saveToDB(){
        Category cat11 = new Category("TEST");
        Product newProduct = new Product("TestProduct","TST-PROD", BigDecimal.valueOf(81230.02));
        newProduct.addCategory(cat11);
        Product DBproduct = productRepository.save(newProduct);
        categoryRepository.findAll().forEach(c->System.out.println(c.getName()));

        assertEquals(newProduct,entityManager.find(Product.class,DBproduct.getId()));
    }





}