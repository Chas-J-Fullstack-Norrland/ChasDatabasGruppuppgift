package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class OrderServiceInventoryTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Test
    void shouldRejectAddingProductNotInStock() {
        Product outOfStockProduct = new Product(
                "Out of Stock Product",
                "SKU-OUT",
                new BigDecimal("50.00")
        );
        outOfStockProduct.setQTY(0); //inget lager
        productRepository.save(outOfStockProduct);
        Product dummyProduct = new Product("Dummy Product", "SKU-DUMMY", new BigDecimal("1.00"));
        dummyProduct.setQTY(10);
        productRepository.save(dummyProduct);
        Order order = new Order();
        OrderItem initialItem = new OrderItem(order, dummyProduct, 1);
        order.getItems().add(initialItem);
        orderRepository.save(order);

        assertThatThrownBy(() -> orderService.addItemToOrder(
                order.getId(),
                outOfStockProduct.getId(),
                1
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not in stock");
    }
}
