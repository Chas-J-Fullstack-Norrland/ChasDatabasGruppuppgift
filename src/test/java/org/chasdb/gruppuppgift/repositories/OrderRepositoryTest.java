package org.chasdb.gruppuppgift.repositories;

import jakarta.transaction.Transactional;
import org.assertj.core.api.BigDecimalAssert;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.chasdb.gruppuppgift.models.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OrderRepositoryTest {
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ProductRepository productRepository;
    @Test
    void shouldPersistOrderWithItemsAndCalculateTotalExactly() {
        //Arrange
        Product product = new Product(
                "Test product",
                "SKU-1",
                new BigDecimal("19.99")
        );
        Product product2 = new Product(
                "Service Product",
                "SKU-3",
                new BigDecimal("100.00")
        );
        productRepository.save(product);
        productRepository.save(product2);
        Order order = new Order();
        OrderItem item1 = new OrderItem(order, product,2);
        OrderItem item2 = new OrderItem(order, product2,1);
        order.getItems().add(item1);
        order.getItems().add(item2);

        //Act
        Order savedOrder = orderRepository.save(order);
        //Assert
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getItems()).hasSize(2);

        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo(order.getTotalPrice());
    }
}
