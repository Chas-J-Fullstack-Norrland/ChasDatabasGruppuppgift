package org.chasdb.gruppuppgift.helpers;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Product;

import org.chasdb.gruppuppgift.services.CustomerService;
import org.chasdb.gruppuppgift.services.ProductService;
import org.chasdb.gruppuppgift.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({CustomerCart.class, CustomerService.class, ProductService.class, ReservationService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")

public class CustomerCartTest {

    @Autowired
    private CustomerCart customerCart;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    private Product testProduct;
    private Customer testCustomer;
    private final String customerEmail = "test@example.com";
    private final String productSku = "SKU-123";

    @BeforeEach
    void setUp() {

        testCustomer = customerService.registerCustomer("Test User", customerEmail);


        testProduct = productService.addProduct("Test Product", productSku, BigDecimal.valueOf(100.00));
        productService.addStockToProduct(productSku, 10);
    }

    @Test
    void testCreateCart() {
        customerCart.create(customerEmail);
        assertNotNull(customerCart.getCustomer());
        assertEquals(customerEmail, customerCart.getCustomer().getEmail());
        assertTrue(customerCart.getItems().isEmpty());
    }

    @Test
    void testAddToCart() {
        customerCart.create(customerEmail);
        customerCart.add(productSku, 2);

        List<CustomerCart.CartItem> items = customerCart.getItems();
        assertEquals(1, items.size());
        assertEquals(productSku, items.get(0).getProduct().getSku());
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    void testSelectCartWithExistingReservations() {

        customerCart.create(customerEmail);
        customerCart.add(productSku, 3);


        customerCart = new CustomerCart(productService, customerService, (ReservationService) null);



        customerCart = (CustomerCart) applicationContext.getBean(CustomerCart.class);


        customerCart.select(customerEmail);

        assertNotNull(customerCart.getCustomer());
        assertEquals(1, customerCart.getItems().size());
        assertEquals(3, customerCart.getItems().get(0).getQuantity());
    }

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Test
    void testRemoveFromCart() {
        customerCart.create(customerEmail);
        customerCart.add(productSku, 5);
        assertEquals(1, customerCart.getItems().size());

        customerCart.remove(productSku);
        assertTrue(customerCart.getItems().isEmpty());
    }
}
