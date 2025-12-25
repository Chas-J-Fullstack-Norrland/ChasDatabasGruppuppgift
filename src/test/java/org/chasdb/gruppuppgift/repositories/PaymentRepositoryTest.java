package org.chasdb.gruppuppgift.repositories;

import org.chasdb.gruppuppgift.models.*;
import org.chasdb.gruppuppgift.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    PaymentRepository repository;
    @Autowired
    OrderRepository orderRepo;
    @Autowired
    ProductRepository productRepo;
    @Autowired
    CustomerRepository customerRepo;

    @Autowired
    TestEntityManager entityManager;



    Payment testPayment;
    Order testOrder;
    Order neworder;
    Product testProduct;
    @BeforeEach
    void setup(){

        Customer c = customerRepo.save(new Customer("testcustomer","testemail"));
        testProduct = productRepo.save(new Product("Test","testprod", BigDecimal.valueOf(2)));
        Order neworder = new Order();
        neworder.addOrderItem(testProduct,1);
        neworder.setCustomer(c);
        testOrder = orderRepo.save(neworder);
        testPayment = repository.save(new Payment("CARD","APPROVED",testOrder));
    }

    @Test
    void shouldSavePaymentToDB(){

        assertEquals(testPayment,entityManager.find(Payment.class,testPayment.getId()));
    }

    @Test
    void findByID(){
        Optional<Payment> fetchedPayment = repository.findById(testPayment.getId());
        assertTrue(fetchedPayment.isPresent());
        assertEquals(testPayment.getId(),fetchedPayment.get().getId());
    }

    @Test
    void updatePayment(){
        testPayment.setMethod("INVOICE");
        Payment paymentToUpdate = repository.save(testPayment);
        assertEquals(testPayment.getMethod(),entityManager.find(Payment.class,paymentToUpdate.getId()).getMethod());
    }

    @Test
    void deletePayment(){
        repository.deleteById(testPayment.getId());
        assertFalse(repository.findById(testPayment.getId()).isPresent());
    }


}