package org.chasdb.gruppuppgift.services;
import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OrderServiceTest {
    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @AfterEach
    void cleanup(){
        orderRepository.deleteAll();
        productRepository.deleteAll();
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
        Product savedProduct = productRepository.save(product);
        Product savedProduct2 = productRepository.save(product2);
        Order orderToSave = new Order();
        orderToSave.addOrderItem(savedProduct, 5);
        //Act
        Order order = orderService.createOrder(orderToSave);
        Order updatedOrder = orderService.addItemToOrder(
                order.getId(),
                savedProduct2.getId(),
                3
        );
        //Assert
        assertThat(updatedOrder.getItems()).hasSize(2);
        assertThat(updatedOrder.getTotalPrice())
                .isEqualByComparingTo(updatedOrder.getTotalPrice());
    }
    @Test
    void shouldRejectZeroQuantity() {
        Product product = new Product(
                "Bad Quantity Product",
                "SKU-4",
                new BigDecimal("10.00")
        );
        Product savedProduct = productRepository.save(product);
        Order orderToSave = new Order();
        orderToSave.addOrderItem(savedProduct, 0);
        assertThatThrownBy(() ->
                orderService.createOrder(orderToSave)
        ).isInstanceOf(DataIntegrityViolationException.class);


    }
}
