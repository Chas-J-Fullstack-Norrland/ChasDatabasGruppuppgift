package org.chasdb.gruppuppgift.helpers;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.cli.AppRunner;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Product;

import org.chasdb.gruppuppgift.repositories.ReservationRepository;
import org.chasdb.gruppuppgift.services.CustomerService;
import org.chasdb.gruppuppgift.services.ProductService;
import org.chasdb.gruppuppgift.services.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CustomerCartTest {

    @MockitoBean
    AppRunner appRunner;
    @Autowired
    private CustomerCart customerCart;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;
    @Autowired
    private ReservationRepository reservationRepository;

    private Product testProduct;
    private Customer testCustomer;
    private final String customerEmail = "test@example.com";
    private final String productSku = "SKU-123";

    @BeforeEach
    void setUp() {
        testCustomer = customerService.registerCustomer("Test User", customerEmail);
        testProduct = productService.addProduct("Test Product", productSku, BigDecimal.valueOf(100.00));
        productService.addStockToProduct(productSku, 10);
        customerCart.create(customerEmail);
    }

    @Test
    @Transactional
    void testCreateCart() {
        customerService.registerCustomer("new user","newEmail@Test.se");
        customerCart.create("newEmail@Test.se");
        assertNotNull(customerCart.getCustomer());
        assertEquals("newEmail@Test.se", customerCart.getCustomer().getEmail());
        assertTrue(customerCart.getItems().isEmpty());
    }

    @Test
    @Transactional
    void testAddToCart() {
        customerCart.add(productSku, 2);

        List<CustomerCart.CartItem> items = customerCart.getItems();
        assertEquals(1, items.size());
        assertEquals(productSku, items.getFirst().getProduct().getSku());
        assertEquals(2, items.getFirst().getQuantity());
    }

    @Test
    @Transactional
    void testSelectCartWithExistingReservations() {

        customerCart.add(productSku, 3);
        customerCart = new CustomerCart(productService, customerService, (ReservationService) null);
        customerCart = (CustomerCart) applicationContext.getBean(CustomerCart.class);


        customerCart.select(customerEmail);

        assertNotNull(customerCart.getCustomer());
        assertEquals(1, customerCart.getItems().size());
        assertEquals(3, customerCart.getItems().getFirst().getQuantity());
    }

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Test
    @Transactional
    void testRemoveFromCart() {
        customerCart.add(productSku, 5);
        assertEquals(1, customerCart.getItems().size());

        customerCart.remove(productSku);
        assertTrue(customerCart.getItems().isEmpty());
    }
}
