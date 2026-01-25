package org.chasdb.gruppuppgift.performance;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.cli.AppRunner;
import org.chasdb.gruppuppgift.models.*;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.repositories.*;
import org.chasdb.gruppuppgift.util.CSVImporter;
import org.chasdb.gruppuppgift.util.CSVPopulateUtils.CsvMapperRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class MediumPerformanceTest {


    @Autowired
    public CSVImporter importer;

    @Autowired
    private EntityManager entityManager;


    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoBean
    AppRunner appRunner;

    @BeforeAll
    public void importScenario() {
        Path path = Paths.get("medium.csv");
        try {
            importer.importCsv(Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void clearEM(){
        entityManager.clear();
    }

    //config
    private int customerCount = 100;
    private int orderCount = 50;
    private int productCount =50;



    @Test
    void find5RandomCustomerByID() {
        Random newRandom = new Random();
        List<Long> randomIDs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            randomIDs.add(newRandom.nextLong(customerCount));
        }
        List<Customer> result = customerRepository.findAllById(randomIDs);
        assertFalse(result.isEmpty());

    }

    @Test
    void find50RandomCustomerByID() {
        Random newRandom = new Random();
        List<Long> randomIDs = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            randomIDs.add(newRandom.nextLong(customerCount));
        }
        List<Customer> result = customerRepository.findAllById(randomIDs);
        assertFalse(result.isEmpty());

    }

    @Test
    void testFindAllCustomers() {
        List<Customer> result = customerRepository.findAll();
        assertFalse(result.isEmpty());

    }

    @Test
    void testFindAllOrdersWithStatus(){
        List<Order> result = orderRepository.findByStatus(OrderStatus.CANCELLED);
        assertFalse(result.isEmpty());
    }

    @Test
    void findAllCategories() {
        List<Category> result = categoryRepository.findAll();
        assertFalse(result.isEmpty());
    }


    @Test
    void find5RandomOrderByID() {
        Random newRandom = new Random();
        List<Long> randomIDs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            randomIDs.add(newRandom.nextLong(orderCount));
        }
        List<Order> result = orderRepository.findAllById(randomIDs);
        assertFalse(result.isEmpty());

    }

    @Test
    void find15RandomOrderByID() {
        Random newRandom = new Random();
        List<Long> randomIDs = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            randomIDs.add(newRandom.nextLong(orderCount));
        }
        List<Customer> result = customerRepository.findAllById(randomIDs);
        assertFalse(result.isEmpty());

    }
    @Test
    void findOrderByCode(){
        Random newRandom = new Random();
        Order result= orderRepository.findByCode("ORD-00030").orElseThrow();
        assertNotNull(result);
    }

    @Test
    void testFindAllOrders() {
        List<Order> result = orderRepository.findAll();
        assertFalse(result.isEmpty());

    }

    @Test
    void findOrderItemsFromRandomOrderCode(){
        Random newRandom = new Random();
        Order result = orderRepository.findByCode("ORD-000"+newRandom.nextInt(orderCount)).orElseThrow();
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    void fetchAllOrderItems(){
        Random newRandom = new Random();
        List<OrderItem> result = orderItemRepository.findAll();
        assertFalse(result.isEmpty());

    }

    @Test
    void find5RandomProductByID() {
        Random newRandom = new Random();
        List<Long> randomIDs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            randomIDs.add(newRandom.nextLong(productCount));
        }
        List<Product> result = productRepository.findAllById(randomIDs);
        assertFalse(result.isEmpty());

    }

    @Test
    void find15RandomProductByID() {
        Random newRandom = new Random();
        List<Long> randomIDs = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            randomIDs.add(newRandom.nextLong(productCount));
        }
        List<Product> result = productRepository.findAllById(randomIDs);
        assertFalse(result.isEmpty());

    }

    @Test
    void testFindAllProducts() {
        List<Customer> result = customerRepository.findAll();
        assertFalse(result.isEmpty());
    }

    @Test
    void findAllProductsOfCategory(){
        List<Product> result = productRepository.findByCategories_Name("Hats");
        assertFalse(result.isEmpty());
    }

    @Test
    void findProductsBySKU(){
        Random newRandom = new Random();
        Product p = productRepository.findBySku("SKU"+newRandom.nextInt(productCount)).orElseThrow();
        assertNotNull(p.getId());
    }

    @Test
    void findProductsWithLowStock(){
        List<Product> result = productRepository.findByInventory_QtyLessThan(3);
        assertFalse(result.isEmpty());

    }

    @Test
    void findCustomerReservations(){
        Random newRandom = new Random();
        List<Reservation> result= reservationRepository.findByCustomerEmail("customer"+newRandom.nextInt(customerCount)+"@example.com");
        assertFalse(result.isEmpty());
    }
    @Test
    void findAllReservations(){
        List<Reservation> result= reservationRepository.findAll();
        assertFalse(result.isEmpty());
    }

    @Test
    void findPaymentFromOrderID(){
        Random newRandom = new Random();
        List<Payment> result= paymentRepository.findAllByOrderId(newRandom.nextLong(orderCount));
        assertFalse(result.isEmpty());
    }

    @Test
    void findallPayments(){
        Random newRandom = new Random();
        List<Payment> result= paymentRepository.findAll();
        assertFalse(result.isEmpty());
    }

}


