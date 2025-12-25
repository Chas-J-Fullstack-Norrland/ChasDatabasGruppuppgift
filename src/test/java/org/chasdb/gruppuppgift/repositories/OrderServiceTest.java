package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.chasdb.gruppuppgift.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;

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
    @Test
    void shouldCreateOrderAndAddItem() {
        //Arrange
        Product product1 = new Product(
                "Service Product",
                "SKU-2",
                new BigDecimal("100.00")
        );
        Product product2 = new Product(
                "Service Product",
                "SKU-3",
                new BigDecimal("100.00")
        );
        product1.setQTY(10);
        product2.setQTY(10);
        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);
        Order orderToSave = new Order();
        orderToSave.addOrderItem(savedProduct1, 5);
        //Act
        Order order = orderService.createOrder(orderToSave);
        Order updatedOrder = orderService.addItemToOrder(
                order.getId(),
                savedProduct2.getId(),
                3
        );
        //Assert
        assertThat(updatedOrder.getItems()).hasSize(2);
        BigDecimal expectedTotal = savedProduct1.getPrice().multiply(BigDecimal.valueOf(5))
                        .add(savedProduct2.getPrice().multiply(BigDecimal.valueOf(3)));
        assertThat(updatedOrder.getTotalPrice())
                .isEqualByComparingTo(expectedTotal);
    }
    @Test
    void shouldRejectZeroQuantity() {
        Product product = new Product(
                "Bad Quantity Product",
                "SKU-3",
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
