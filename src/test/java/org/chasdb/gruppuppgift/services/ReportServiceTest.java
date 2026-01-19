package org.chasdb.gruppuppgift.services;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.cli.AppRunner;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.dto.DailyRevenueDTO;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReportServiceTest {
    @MockitoBean
    AppRunner appRunner;
    @Autowired
    ReportService reportService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CustomerService customerService;


    @Test
    @Transactional
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

        var result = reportService.getTopSellingProducts(6);
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().totalSold()).isEqualTo(5);
    }
    @Test
    @Transactional
    void shouldFindLowStockProducts() {
        Product low = new Product("Low", "SKU-LOW", new BigDecimal("5"));
        productRepository.save(low);
        List<Product> products = reportService.getLowStockProducts(5);

        assertThat(products)
                .extracting(Product::getSku)
                .contains("SKU-LOW");
    }
    @Test
    @Transactional
    void shouldCalculateRevenuePerDay() {
        Customer c = customerService.registerCustomer("Testing Customer","Testing_cus@1992.wq");
        Product p = productRepository.save(
                new Product("Rev", "REV", new BigDecimal("100"))
        );
        Order order = new Order();
        order.setCustomer(c);
        order.addOrderItem(p, 2); //200
        order.setTotal_Price(order.calculatePriceOfProducts());
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        List<DailyRevenueDTO> report = reportService.getRevenueReport(LocalDate.now().minusDays(1),LocalDate.now().plusDays(1));
        Map<LocalDate,BigDecimal> revenueMap = new HashMap<>();
        for ( DailyRevenueDTO item : report){
            revenueMap.put(item.date(),item.totalRevenue());
        }
        assertEquals(BigDecimal.valueOf(200.00).setScale(2),revenueMap.get(order.getCreatedAt().toLocalDate()));
    }
}
