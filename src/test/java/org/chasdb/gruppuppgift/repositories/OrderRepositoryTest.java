package org.chasdb.gruppuppgift.repositories;

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
        productRepository.save(product);
        Order order = new Order();
        OrderItem item1 = new OrderItem(order, product,2, product.getPrice());
        OrderItem item2 = new OrderItem(order, product,1, product.getPrice());
        order.getItems().add(item1);
        order.getItems().add(item2);
        //Act
        Order savedOrder = orderRepository.save(order);
        //Assert
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getItems()).hasSize(2);

        BigDecimal expectedTotal = new BigDecimal("59.97");
        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }
}
