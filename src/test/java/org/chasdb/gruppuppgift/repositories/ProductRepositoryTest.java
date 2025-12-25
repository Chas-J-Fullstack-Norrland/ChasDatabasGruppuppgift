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
import java.util.Optional;

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
    InventoryRepository inventoryRepository;
    @Autowired
    TestEntityManager entityManager;

    List<Category> categories;
    Product DBproduct;
    Product Product_in_Stock;

    @BeforeEach
    void setup(){
        Category cat1 = new Category("TestCategory1");
        Category cat2 = new Category("CategoryTest2");
        categories = categoryRepository.saveAll(List.of(cat1,cat2));
        Product newProduct = new Product("TestProduct","TST-PROD", BigDecimal.valueOf(81230.02));
        Product newProduct2 = new Product("TestProduct2","TST-PROD2", BigDecimal.valueOf(80.02));
        newProduct.addCategory(categories.getFirst());

        DBproduct = productRepository.save(newProduct);
        Product_in_Stock = productRepository.save(newProduct2);
        inventoryRepository.updateQuantityBySku(DBproduct.getSku(), 2);
        inventoryRepository.updateQuantityBySku(Product_in_Stock.getSku(), 20);
    }

    @Test
    void shouldSaveProductToDB(){
        Category cat11 = new Category("TEST");
        Product newProduct = new Product("TestProduct3","TST-PROD3", BigDecimal.valueOf(81230.02));
        newProduct.addCategory(cat11);
        Product DBproduct = productRepository.save(newProduct);
        categoryRepository.findAll().forEach(c->System.out.println(c.getName()));

        assertEquals(newProduct,entityManager.find(Product.class,DBproduct.getId()));
    }

    @Test
    void findProductByID(){
        Optional<Product> fetchedProduct = productRepository.findById(DBproduct.getId());
        assertTrue(fetchedProduct.isPresent());
        assertEquals(DBproduct.getName(),fetchedProduct.get().getName());
    }

    @Test
    void shouldUpdateProduct(){
        DBproduct.setName("UPDATED");
        Product categoryToUpdate = productRepository.save(DBproduct);
        assertEquals(DBproduct.getName(),entityManager.find(Product.class,categoryToUpdate.getId()).getName());
    }

    @Test
    void shouldDeleteProduct(){
        productRepository.deleteById(DBproduct.getId());
        assertFalse(productRepository.existsById(DBproduct.getId()));
    }

    @Test
    void ShouldListAllProductsOfCategory(){
        assertFalse(productRepository.findByCategories_Name(categories.getFirst().getName()).isEmpty());
        assertTrue(productRepository.findByCategories_Name(categories.getLast().getName()).isEmpty());
    }

    @Test
    void ShouldFindAllProductsBelowQTYInventory(){
        List<Product> productStocks = productRepository.findByInventory_QtyLessThan(5);
        assertFalse(productStocks.size()>1);
        assertTrue(productStocks.getFirst().getName().equals(DBproduct.getName()));
    }

    @Test
    void canDisableAndEnableProduct(){
        assertTrue(DBproduct.isActive());
        productRepository.disableProduct(DBproduct.getId());

        assertFalse(entityManager.find(Product.class,DBproduct.getId()).isActive());
        productRepository.enableProduct(DBproduct.getId());
        assertTrue(entityManager.find(Product.class,DBproduct.getId()).isActive());


    }








}