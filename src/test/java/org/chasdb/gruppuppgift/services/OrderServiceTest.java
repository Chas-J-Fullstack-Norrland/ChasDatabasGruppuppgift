package org.chasdb.gruppuppgift.services;
import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.CustomerRepository;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.PaymentRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OrderServiceTest {
    @Autowired
    OrderService orderService;

    @Autowired
    CustomerService customerService;
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;
    @Autowired
    PaymentRepository paymentRepository;

    @AfterEach
    void cleanup(){
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

    }




    @Test
    void shouldCreateOrderAndAddItem() {
        //Arrange
        Product product = new Product(
                "Service Product",
                "SKU-2",
                new BigDecimal("100.00")
        );
        Product product2 = new Product(
                "Service Product",
                "SKU-3",
                new BigDecimal("100.00")
        );
        Customer c = customerService.registerCustomer("Testname","Email1@test.se");
        Product savedProduct = productRepository.save(product);
        Product savedProduct2 = productRepository.save(product2);
        Order orderToSave = new Order();
        orderToSave.setCustomer(c);
        orderToSave.addOrderItem(savedProduct, 5);
        orderToSave.setTotal_Price(orderToSave.calculatePriceOfProducts());
        //Act
        Order order = orderService.createOrder(orderToSave);
        Order updatedOrder = orderService.addItemToOrder(
                order.getId(),
                savedProduct2.getId(),
                3
        );
        //Assert
        assertThat(updatedOrder.getItems()).hasSize(2);
        assertThat(updatedOrder.getTotal_Price())
                .isEqualByComparingTo(updatedOrder.getTotal_Price());
    }
    @Test
    void shouldRejectZeroQuantity() {
        Product product = new Product(
                "Bad Quantity Product",
                "SKU-4",
                new BigDecimal("10.00")
        );
        Customer c = customerService.registerCustomer("Testname","Email2@test.se");
        Product savedProduct = productRepository.save(product);
        Order orderToSave = new Order();
        orderToSave.setCustomer(c);
        orderToSave.addOrderItem(savedProduct, 0);
        assertThatThrownBy(() ->
                orderService.createOrder(orderToSave)
        ).isInstanceOf(DataIntegrityViolationException.class);


    }

    @Test
    void shouldSuccessfullyCompleteTransaction(){
        Product product = new Product(
                "Bad Quantity Product",
                "SKU-4",
                new BigDecimal("10.00")
        );
        Customer c = customerService.registerCustomer("Testname","Email23@test.se");
        Product savedProduct = productRepository.save(product);
        List<Product> savedProducts = List.of(savedProduct);

        Order savedOrder = orderService.checkout(c,savedProducts,"CARD");

        assertTrue(savedOrder.getStatus().equals("PAID"));


    }

}
