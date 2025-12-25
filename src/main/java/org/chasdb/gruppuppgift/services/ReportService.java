package org.chasdb.gruppuppgift.services;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.OrderItemRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;


import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    /** Topp 5 Bästsäljare*/
    public List<ProductSalesDTO> getTop5BestSellers() {
        return orderItemRepository
                .findTopSellingProducts(PageRequest.of(0, 5))
                .stream()
                .map(r -> new ProductSalesDTO(
                        (Product) r[0],
                        ((Long) r[1]).intValue()
                ))
                        .toList();
    }
    /** Produkter med Lågt lager*/
    public List<Product> getLowStockProducts(int limit) {
        return productRepository.findByInventory_QtyLessThan(limit);
    }
    /** Omsättning per dag */
    public Map<LocalDate, BigDecimal> getRevenuePerDay() {
        return orderItemRepository.findRevenuePerDay()
                .stream()
                .collect(Collectors.toMap(
                        r -> (LocalDate) r[0],
                        r -> (BigDecimal) r[1]
                ));
    }
}
