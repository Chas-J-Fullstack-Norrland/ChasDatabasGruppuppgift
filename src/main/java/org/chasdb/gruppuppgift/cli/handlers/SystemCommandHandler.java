package org.chasdb.gruppuppgift.cli.handlers;

import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.repositories.*;
import org.chasdb.gruppuppgift.services.ProductService;
import org.chasdb.gruppuppgift.util.CSVImporter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class SystemCommandHandler implements CommandHandler {
    private final ProductService productService;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final CategoryRepository categoryRepository;

    public SystemCommandHandler(ProductService productService,
                                OrderRepository orderRepository,
                                ProductRepository productRepository,
                                CustomerRepository customerRepository,
                                InventoryRepository inventoryRepository,
                                PaymentRepository paymentRepository,
                                ReservationRepository reservationRepository,
                                CategoryRepository categoryRepository) {
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public String getDomain() {
        return "system";
    }

    @Override
    public void handle(CommandInput input) {
        switch (input.action()) {
            case "import" -> handleImport(input);
            case "reset" -> handleReset();
            case "help" -> printHelp();
            default -> System.out.println("Okänd system-åtgärd: " + input.action());
        }
    }

    private void handleImport(CommandInput input) {
        String filePath = input.flags().get("file");

        if (filePath == null) {
            System.out.println("Fel: Du måste ange --file=<sökväg>");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Fel: Filen hittades inte: " + filePath);
            return;
        }

        System.out.println("Påbörjar import av: " + filePath);

        try (FileInputStream fis = new FileInputStream(file)) {
            CSVImporter.ImportResult result = productService.importProducts(fis);

            System.out.println("Import klar");
            System.out.println("Lyckades: " + result.successCount());
            System.out.println("Misslyckades: " + result.failureCount());

            if (result.hasErrors()) {
                System.out.println("--- FEL: ---");
                result.errors().forEach(System.out::println);
            }
        } catch (IOException e) {
            System.out.println("Kritiskt IO-fel: " + e.getMessage());
        }
    }

    @Transactional
    protected void handleReset() {
        System.out.println("VARNING: Rensar hela databasen...");

        try {
            paymentRepository.deleteAll();
            reservationRepository.deleteAll();

            orderRepository.deleteAll();

            inventoryRepository.deleteAll();
            productRepository.deleteAll();

            customerRepository.deleteAll();
            categoryRepository.deleteAll();

            System.out.println("Databasen är nu helt tom!");

        } catch (Exception e) {
            System.out.println("Kunde inte återställa databasen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printHelp() {
        System.out.println("\n--- TILLGÄNGLIGA KOMMANDON ---");
        System.out.println("Syntax: [domain] [action] [flags]");

        System.out.println("\nGenerellt:");
        System.out.println("  exit                                           - Avsluta programmet");
        System.out.println("  help                                           - Visa denna lista");

        System.out.println("\nSystem:");
        System.out.println("  system import --file=<sökväg>                  - Importera produkter från CSV");
        System.out.println("  system reset                                   - Töm hela databasen (VARNING!)");

        System.out.println("\nProdukter:");
        System.out.println("  product list                                   - Visa alla produkter i lager");
        System.out.println("  product search --q=<sökord>                    - Sök efter en produkt");

        System.out.println("\nVarukorg & Order:");
        System.out.println("  cart add --email=<mail> --sku=<sku> --qty=<n>  - Lägg produkt i varukorgen");
        System.out.println("  cart show --email=<mail>                       - Visa innehållet i varukorgen");
        System.out.println("  cart checkout --email=<mail>                   - Gå till kassan och skapa order");

        System.out.println("\nRapporter:");
        System.out.println("  report revenue                                 - Visa total försäljning per dag");
        System.out.println("  report top                                     - Visa topp 10 mest sålda produkter");

        System.out.println("\n------------------------------");
    }
}
