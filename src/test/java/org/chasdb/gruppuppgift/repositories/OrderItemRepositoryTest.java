package org.chasdb.gruppuppgift.repositories;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.chasdb.gruppuppgift.models.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OrderItemRepositoryTest {
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ProductRepository productRepository;

    @Test
    void shouldRejectZeroQuantityAtDatabaseLevel() {
        // arrange
        Product product = new Product(
                "Invalid Quantity Product",
                "SKU-BAD",
                new BigDecimal("10.00")
        );
        productRepository.save(product);
        Order order = new Order();
        OrderItem item = new OrderItem(order, product, 0); //Invalid
        order.addOrderItem(item);


        //Act + Assert
        assertThatThrownBy(() ->
                orderRepository.saveAndFlush(order)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
