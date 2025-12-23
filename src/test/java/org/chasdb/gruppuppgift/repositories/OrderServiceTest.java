package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.chasdb.gruppuppgift.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles
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
        Product product = new Product(
                "Service Product",
                "SKU-2",
                new BigDecimal("100.00")
        );
        productRepository.save(product);
        //Act
        Order order = orderService.createOrder();
        Order updatedOrder = orderService.addItemToOrder(
                order.getId(),
                product.getId(),
                3
        );
        //Assert
        assertThat(updatedOrder.getItems()).hasSize(1);
        assertThat(updatedOrder.getTotalPrice())
                .isEqualByComparingTo(new BigDecimal("300.00"));
    }
    @Test
    void shouldRejectZeroQuantity() {
        Product product = new Product(
                "Bad Quantity Product",
                "SKU-3",
                new BigDecimal("10.00")
        );
        productRepository.save(product);
        Order order = orderService.createOrder();
        assertThatThrownBy(() ->
                        orderService.addItemToOrder(
                                order.getId(),
                                product.getId(),
                                0
        )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be > 0");
    }
}
