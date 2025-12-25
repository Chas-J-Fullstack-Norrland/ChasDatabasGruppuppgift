package org.chasdb.gruppuppgift.cli.handlers;

import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.services.ProductService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCommandHandler implements CommandHandler {
    private final ProductService productService;

    public ProductCommandHandler(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public String getDomain() {
        return "product";
    }

    @Override
    public void handle(CommandInput input) {
        switch (input.action()) {
            case "list" -> handleList(input);
            case "add" -> System.out.println("Använd system import för att lägga till just nu");
            case "disable" -> handleDisable(input);
            default -> System.out.println("Okänd produkt-åtgärd.");
        }
    }

    private void handleList(CommandInput input) {
        List<Product> products;

        if (input.flags().containsKey("category")) {
           String cat = input.flags().get("category");
           products = productService.listProductsByCategories_name(cat);
            System.out.println("Visar produkter i kategori: " + cat);
        } else {
            products = productService.listAllProducts();
            System.out.println("Visar alla produkter:");
        }

        System.out.printf("%-10s %-30s %-10s%n", "SKU", "Namn", "Pris");
        System.out.println("----------------------------------------------------");
        for (Product p : products) {
            System.out.printf("%-10s %-30s %-10s%n", p.getSku(), p.getName(), p.getPrice());
        }
    }

    private void handleDisable(CommandInput input) {
        String sku = input.rawArgs().trim();
        if (sku.isEmpty()) {
            System.out.println("Du måste ange en SKU.");
            return;
        }

        System.out.println("Inaktiverar " + sku + " (Logik behöver kopplas till service)");
    }
}
