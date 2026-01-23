package org.chasdb.gruppuppgift.repositories;

import jakarta.transaction.Transactional;
import org.assertj.core.api.BigDecimalAssert;
import org.chasdb.gruppuppgift.models.Customer;
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
    @Autowired
    CustomerRepository customerRepository;
    @Test
    void shouldPersistOrderWithItemsAndCalculateTotalExactly() {
        //Arrange
        Customer customer = customerRepository.save(new Customer("TestCustomer","TestEmail"));

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
        order.setCustomer(customer);
        OrderItem item1 = new OrderItem(order, product,2);
        OrderItem item2 = new OrderItem(order, product2,1);
        order.addOrderItem(item1);
        order.addOrderItem(item2);
        order.setTotal_Price(order.calculatePriceOfProducts());

        //Act
        Order savedOrder = orderRepository.save(order);
        //Assert
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getItems()).hasSize(2);

        assertThat(savedOrder.getTotal_Price()).isEqualByComparingTo(order.getTotal_Price());
    }
}
