package org.chasdb.gruppuppgift.services;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Payment;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    OrderRepository orderRepo;
    @Autowired
    ProductRepository productRepo;

    @AfterEach
    void cleanup(){

    }

    Product testProduct;
    Order testOrder;
    Payment testPayment;

    @BeforeEach
    void setup(){
        testProduct = productRepo.save(new Product("Test","testprod", BigDecimal.valueOf(2)));
        Order neworder = new Order();
        neworder.addOrderItem(testProduct,1);
        testOrder = orderRepo.save(neworder);
        testPayment = paymentService.savePayment("INVOICE","PENDING",testOrder);
    }

    @Test
    @Transactional
    void payInvoiceSuccessfulTest(){
        Payment newPayment = new Payment();
        int attempts = 0;
        boolean repeatUntilSuccessfulPayment = true;
        while(repeatUntilSuccessfulPayment && attempts < 5){
            repeatUntilSuccessfulPayment = false;
            try{
                newPayment = paymentService.invoicePay(testPayment.getId());
            } catch (Exception e) {
                repeatUntilSuccessfulPayment = true;
                attempts++;
            }
        }
        assertFalse(repeatUntilSuccessfulPayment);
        assertEquals("APPROVED",paymentService.findByID(newPayment.getId()).get().getStatus());
    }


}