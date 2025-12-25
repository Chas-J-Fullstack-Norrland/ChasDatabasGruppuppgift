package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @Autowired
    CustomerService customerService;


    @Test
    void shouldReturnTop5BestSellers() {
        Customer c = customerService.registerCustomer("Testmail","newEmail@live.se");
        Product p1 = productRepository.save(
                new Product("P1", "SKU1", new BigDecimal("10"))
        );
        Product p2 = productRepository.save(
                new Product("P2", "SKU2", new BigDecimal("20"))
        );
        Order order = new Order();
        order.setCustomer(c);
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
        productRepository.save(low);
        List<Product> products = reportService.getLowStockProducts(5);

        assertThat(products)
                .extracting(Product::getSku)
                .contains("SKU-LOW");
    }
    @Test
    void shouldCalculateRevenuePerDay() {
        Customer c = customerService.registerCustomer("Testing Customer","Testing_cus@1992.wq");
        Product p = productRepository.save(
                new Product("Rev", "REV", new BigDecimal("100"))
        );
        Order order = new Order();
        order.setCustomer(c);
        order.addOrderItem(p, 2); //200
        orderRepository.save(order);

        Map<LocalDate,BigDecimal> report = reportService.getRevenuePerDay();
        assertEquals(BigDecimal.valueOf(290),report.get(order.getCreatedAt()));
    }
}
