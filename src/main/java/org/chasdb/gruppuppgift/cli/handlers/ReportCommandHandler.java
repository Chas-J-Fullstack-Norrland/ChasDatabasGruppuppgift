package org.chasdb.gruppuppgift.cli.handlers;

import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.dto.DailyRevenueDTO;
import org.chasdb.gruppuppgift.models.dto.TopProductDTO;
import org.chasdb.gruppuppgift.services.ReportService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class ReportCommandHandler implements CommandHandler {
    private final ReportService reportService;

    public ReportCommandHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public String getDomain() {
        return "report";
    }

    @Override
    public void handle(CommandInput input) {
        switch (input.action()) {
            case "top-products" -> handleTopProducts(input);
            case "low-stock" -> handleLowStock(input);
            case "revenue" -> handleRevenue(input);
            default -> System.out.println("Okänd rapport. Tillgängliga: top-products, low-stock, revenue");
        }
    }

    private void handleTopProducts(CommandInput input) {
        System.out.println("\n--- BÄSTSÄLJARE (TOP 5) ---");
        List<TopProductDTO> topList = reportService.getTopSellingProducts(5);

        if (topList.isEmpty()) {
            System.out.println("Ingen försäljningsdata än.");
            return;
        }

        System.out.printf("%-10s %-25s %-10s%n", "Antal", "Produkt", "Pris");
        System.out.println("------------------------------------------------");
        for (TopProductDTO dto : topList) {
            System.out.printf("%-10d %-25s %-10s%n",
                    dto.totalSold(),
                    truncate(dto.product().getName(), 24),
                    dto.product().getPrice());
        }
    }

    private void handleLowStock(CommandInput input) {
        int limit = 5; // Default gräns
        if (input.flags().containsKey("lt")) {
            try {
                limit = Integer.parseInt(input.flags().get("lt"));
            } catch (NumberFormatException e) {
                System.out.println("Fel: --lt måste vara en siffra.");
                return;
            }
        }

        System.out.println("\n--- LÅGT LAGER (< " + limit + " st) ---");
        List<Product> lowStock = reportService.getLowStockProducts(limit);

        if (lowStock.isEmpty()) {
            System.out.println("Inga produkter har lågt lager.");
            return;
        }

        System.out.printf("%-15s %-25s %-10s%n", "SKU", "Produkt", "Antal");
        System.out.println("------------------------------------------------");
        for (Product p : lowStock) {
            System.out.printf("%-15s %-25s %-10d%n",
                    p.getSku(),
                    truncate(p.getName(), 24),
                    p.getQty());
        }
    }

    private void handleRevenue(CommandInput input) {
        LocalDate from = LocalDate.now().minusMonths(1); // Default: Senaste månaden
        LocalDate to = LocalDate.now();

        try {
            if (input.flags().containsKey("from"))
                from = LocalDate.parse(input.flags().get("from"));

            if (input.flags().containsKey("to"))
                to = LocalDate.parse(input.flags().get("to"));

        } catch (DateTimeParseException e) {
            System.out.println("Fel datumformat. Använd YYYY-MM-DD.");
            return;
        }

        System.out.println("\n--- OMSÄTTNING (" + from + " till " + to + ") ---");
        List<DailyRevenueDTO> revenueList = reportService.getRevenueReport(from, to);

        if (input.flags().containsKey("export")) {
            reportService.exportRevenueReportToCSV(revenueList);
            return;
        }

        if (revenueList.isEmpty()) {
            System.out.println("Ingen försäljning (betald) under perioden.");
            return;
        }

        System.out.printf("%-15s %-15s%n", "Datum", "Total");
        System.out.println("------------------------------");
        for (DailyRevenueDTO dto : revenueList) {
            System.out.printf("%-15s %-15s kr%n", dto.date(), dto.totalRevenue());
        }


    }

    private String truncate(String str, int width) {
        if (str.length() <= width) return str;
        return str.substring(0, width - 3) + "...";
    }
}
