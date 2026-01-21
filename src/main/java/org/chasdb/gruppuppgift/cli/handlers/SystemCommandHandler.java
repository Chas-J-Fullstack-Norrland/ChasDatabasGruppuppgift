package org.chasdb.gruppuppgift.cli.handlers;

import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.repositories.*;
import org.chasdb.gruppuppgift.services.ProductService;
import org.chasdb.gruppuppgift.util.CSVGenerator;
import org.chasdb.gruppuppgift.util.CSVImporter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    private final CSVImporter csvImporter;

    public SystemCommandHandler(ProductService productService,
                                OrderRepository orderRepository,
                                ProductRepository productRepository,
                                CustomerRepository customerRepository,
                                InventoryRepository inventoryRepository,
                                PaymentRepository paymentRepository,
                                ReservationRepository reservationRepository,
                                CategoryRepository categoryRepository, CSVImporter csvImporter) {
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.categoryRepository = categoryRepository;
        this.csvImporter = csvImporter;
    }

    @Override
    public String getDomain() {
        return "system";
    }

    @Override
    public void handle(CommandInput input) {
        switch (input.action()) {
            case "import" -> importCSV(input);
            case "reset" -> handleReset();
            case "help" -> printHelp();
            case "csvgenerate" -> csvGenerate(input);
            default -> System.out.println("Okänd system-åtgärd: " + input.action());
        }
    }

    private void importCSV(CommandInput input){

        if(!input.flags().containsKey("file") || input.flags().get("file").isBlank()){
                System.out.println("Mandatory flag --file=<path> not set");
                return;
        }

        Path path = Paths.get(input.flags().get("file"));
        try{
            csvImporter.importCsv(Files.newInputStream(path));
        } catch (IOException e) {
            System.err.println("Could not read file");
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
        System.out.println("Syntax: domain <action> [--flags] [args]");

        System.out.println("\nGenerellt:");
        System.out.println("  exit                                           - Avsluta programmet");
        System.out.println("  help                                           - Visa denna lista");

        System.out.println("\nSystem:");
        System.out.println("  system import --file=<sökväg>                  - Importera produkter från CSV");
        System.out.println("  system reset                                   - Töm hela databasen (VARNING!)");

        System.out.println("\n  Produkter:");
        System.out.println("    product add                                     - Sök efter en produkt"+
                            "\n     --name=<sökord> --sku=<identifier> --price=<0.0>  "+
                            "\n     [--description] [args=categories] ");
        System.out.println("  product list                                   - Visa alla produkter i lager");
        System.out.println("  product search --q=<sökord>                    - Sök efter en produkt");

        System.out.println("\nVarukorg & Order:");
        System.out.println("  cart select --customer=<email>                        - Välj befintlig kundvagn ");
        System.out.println("  cart create --customer=<email>                        - Skapa kundvagn");
        System.out.println("  cart remove--sku=<sku>                                - Ta bort produkt från varukorgen");
        System.out.println("  cart add --email=<email> --sku=<sku> --qty=<n>        - Lägg produkt i varukorgen");
        System.out.println("  cart show --email=<email>                             - Visa innehållet i varukorgen");
        System.out.println("  cart checkout --email=<email>                         - Gå till kassan och skapa order");

        System.out.println("\nRapporter:");
        System.out.println("  report revenue --from=<YYYY-MM-DD> -to=<YYYY-MM-DD    - Visa total försäljning per dag");
        System.out.println("  report top                                            - Visa topp 10 mest sålda produkter");
        System.out.println("  report low-stock --lt=<limit>                         - Visa producter med lågt inventarie");

        System.out.println("\nOrdrar:");
        System.out.println("  order list [--status=PAID]                      - Visa ordrar");
        System.out.println("  order show <id>                                 - Visa detaljer för en order");
        System.out.println("  order cancel <id>                               - Makulera order & återställ lager (Retur)"); // <--- NYTT

        System.out.println("\n------------------------------");
    }

    private void csvGenerate(CommandInput input){
        CSVGenerator generator = new CSVGenerator() ;
        String filename = "csvfile";

        generator.setVariant(Integer.parseInt(input.flags().get("variant")));

        if(input.flags().containsKey("file")){
                filename = input.flags().get("file");
        }

        try{
            generator.GenerateCSV(filename);
        } catch (IOException e) {
            System.err.println("Something went wrong");
        }



    }

}
