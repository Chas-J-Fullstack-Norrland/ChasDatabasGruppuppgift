package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.services.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReportServiceTest {
    @Autowired
    ReportService reportService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;
    @Test
    void shouldReturnTop5BestSellers() {
        Product p1 = productRepository.save(
                new Product("P1", "SKU1", new BigDecimal("10"))
        );
        Product p2 = productRepository.save(
                new Product("P2", "SKU2", new BigDecimal("20"))
        );
        Order order = new Order();
        order.addOrderItem(p1, 5);
        order.addOrderItem(p2, 2);
        orderRepository.save(order);

        var result = reportService.getTop5BestSellers();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().quantitySold()).isEqualTo(5);
    }
    @Test
    void shouldFindLowStockProducts() {
        Product low = new Product("Low", "SKU-LOW", new BigDecimal("5"));
        low.setQTY(1);
        productRepository.save(low);
        List<Product> products = reportService.getLowStockProducts(5);

        assertThat(products)
                .extracting(Product::getSku)
                .contains("SKU-LOW");
    }
    @Test
    void shouldCalculateRevenuePerDay() {
        Product p = productRepository.save(
                new Product("Rev", "REV", new BigDecimal("100"))
        );
        Order order = new Order();
        order.addOrderItem(p, 2); //200
        orderRepository.save(order);

        var revenue = reportService.getRevenuePerDay();
        assertThat(revenue.values())
                .contains(new BigDecimal("290.00"));
    }
}
