package org.chasdb.gruppuppgift;

import org.chasdb.gruppuppgift.cli.AppRunner;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.repositories.*;
import org.chasdb.gruppuppgift.services.CartServiceContract;
import org.chasdb.gruppuppgift.services.OrderService;
import org.chasdb.gruppuppgift.services.ProductService;
import org.chasdb.gruppuppgift.util.CSVImporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ECommerceScenarioTest {
    @MockitoBean
    AppRunner appRunner;
    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private CartServiceContract cartService;
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderItemRepository orderItemRepository;


    @MockitoBean
    AppRunner runner;

    @AfterEach
     void cleanup() {
         paymentRepository.deleteAll();
         reservationRepository.deleteAll();
         orderItemRepository.deleteAll();
         orderRepository.deleteAll();
         productRepository.deleteAll();
         customerRepository.deleteAll();
     }


    // --- SCENARIO 1: LITET (Import + Enkelt köp) ---
    @Test //Arguably shouldnt be transactional due to import behavior
    @DisplayName("Scenario 1: Import av produkter och ett vanligt köp")

    void testScenario1_ImportAndBuy() {
        // Skapa CSV-data
        String csvContent = """
                name,sku,price,description
                "Nike Air",SKU-NIKE-001,1200.50,"Supersnabba skor"
                "Adidas Run",SKU-ADI-002,999.00,"Bra för asfalt"
                """;
        InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        // Importera
        CSVImporter.ImportResult result = productService.importProducts(is);
        assertEquals(2, result.successCount());

        // Fyll på lagret
        productRepository.findAll().forEach(p -> {
            p.setQty(5);
            productRepository.save(p);
        });

        // Skapa kund
        Customer customer = customerRepository.save(new Customer("Kalle", "kalle@test.se"));

        // Handla
        cartService.addToCart(customer.getEmail(), "SKU-NIKE-001", 1);
        cartService.addToCart(customer.getEmail(), "SKU-ADI-002", 2);

        // Checka ut
        boolean bought = false;
        for (int i = 0; i < 10; i++) {
            try {
                orderService.processCheckout(customer.getEmail(), PaymentMethod.CARD);
                bought = true;
                break;
            } catch (Exception e) {
                System.out.println("Betalning nekad, försöker igen...");
            }
        }
        assertTrue(bought, "Köp borde ha lyckats inom 10 försök");
        assertEquals(1, orderRepository.count(), "En order ska ha skapats");
    }

    // --- SCENARIO 2: MELLAN (Concurrency / Race Condition) ---
    @Test
    @DisplayName("Scenario 2: Race Condition - 5 kunder slåss om 1 vara i varukorgen")
    void testScenario2_Concurrency() throws InterruptedException {
        // Skapa en produkt med saldo 1 (SISTA EXEMPLARET!)
        Product p = new Product("Guldklocka", "SKU-GOLD", new BigDecimal(5000));
        p.setQty(1); // Bara 1 i lager
        productRepository.save(p);

        // Skapa 5 kunder
        int customerCount = 5;
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < customerCount; i++) {
            customers.add(customerRepository.save(new Customer("Cust" + i, "race" + i + "@test.com")));
        }

        ExecutorService executor = Executors.newFixedThreadPool(customerCount);
        CountDownLatch latch = new CountDownLatch(customerCount);
        AtomicInteger successfulReservations = new AtomicInteger(0);

        for (Customer c : customers) {
            executor.submit(() -> {
                try {
                    // Försök lägga i varukorgen (Detta reserverar varan)
                    cartService.addToCart(c.getEmail(), "SKU-GOLD", 1);
                    successfulReservations.incrementAndGet();
                } catch (Exception e) {
                    // System.out.println("Reservation misslyckades för " + c.getEmail());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Verifiera resultatet
        System.out.println("Antal lyckade reservationer: " + successfulReservations.get());

        // Endast EN kund ska ha lyckats lägga den i varukorgen
        assertEquals(1, successfulReservations.get(), "Endast EN kund ska lyckas paxa varan!");

        // Kontrollera att lagret är 0 (reserverat)
        Inventory inv = inventoryRepository.findByProduct_Sku("SKU-GOLD").orElseThrow();
        assertEquals(0, inv.getQty(), "Lagret ska vara 0");
    }

    // --- SCENARIO 3: STORT (Stress Test) ---
    @Test
    @DisplayName("Scenario 3: Stress Test - 1000 produkter, 2000 ordrar")
    @Transactional
    void testScenario3_StressTest() {
        long startTime = System.currentTimeMillis();

        // Generera 1000 produkter
        System.out.println("Genererar produkter...");
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Product p = new Product("Prod-" + i, "SKU-" + i, new BigDecimal(100 + i));
            p.setQty(10000); // Oändligt lager för testet
            products.add(p);
        }
        products = productRepository.saveAll(products);

        // Generera 100 kunder
        System.out.println("Genererar kunder...");
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            customers.add(customerRepository.save(new Customer("Stress" + i, "stress" + i + "@test.com")));
        }


        // Simulera 2000 köp
        System.out.println("Kör 2000 köp...");
        Random random = new Random();
        int successfulOrders = 0;

        for (int i = 0; i < 2000; i++) {
            Customer c = customers.get(random.nextInt(customers.size()));
            Product p = products.get(random.nextInt(products.size()));

            // Lägg i varukorg
            cartService.addToCart(c.getEmail(), p.getSku(), 1);

            // Försök köpa
            try {
                orderService.processCheckout(c.getEmail(), PaymentMethod.INVOICE);
                successfulOrders++;
            } catch (Exception e) {}
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("--- STRESS TEST RESULTAT ---");
        System.out.println("Tid: " + duration + " ms");
        System.out.println("Lyckade ordrar: " + successfulOrders);
        System.out.println("Rader i Order-tabellen: " + orderRepository.count());

        assertTrue(orderRepository.count() > 0, "Det ska finnas ordrar i databasen");
    }
}