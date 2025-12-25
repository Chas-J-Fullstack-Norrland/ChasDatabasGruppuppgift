package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.dto.DailyRevenueDTO;
import org.chasdb.gruppuppgift.models.dto.TopProductDTO;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;
import org.chasdb.gruppuppgift.repositories.OrderItemRepository;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {
    private final OrderItemRepository orderItemRepo;
    private final InventoryRepository inventoryRepo;
    private final OrderRepository orderRepo;

    public ReportService(OrderItemRepository orderItemRepo,
                         InventoryRepository inventoryRepo,
                         OrderRepository orderRepo) {
        this.orderItemRepo = orderItemRepo;
        this.inventoryRepo = inventoryRepo;
        this.orderRepo = orderRepo;
    }
    public List<TopProductDTO> getTopSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> results = orderItemRepo.findTopSellingProductsRaw(pageable);

        return results.stream()
                .map(row -> {
                    Product product = (Product) row[0];
                    Long count = (Long) row[1];
                    return new TopProductDTO(product, count);
                })
                .collect(Collectors.toList());
    }

    public List<Inventory> getLowStockProducts(int threshold) {
        return inventoryRepo.findByQuantityLessThan(threshold);
    }

    public List<DailyRevenueDTO> getRevenueReport(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        List<Object[]> rawData = orderRepo.getDailyRevenue(start, end);

        return rawData.stream()
                .map(row -> {
                    Date sqlDate = (Date) row[0];
                    BigDecimal total = (BigDecimal) row[1];

                    return new DailyRevenueDTO(sqlDate.toLocalDate(), total);
                })
                .collect(Collectors.toList());
    }
}
