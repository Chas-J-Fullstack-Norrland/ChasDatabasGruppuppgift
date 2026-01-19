package org.chasdb.gruppuppgift.services;

import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.cli.AppRunner;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Payment;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.models.enums.PaymentStatus;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PaymentServiceTest {

    @MockitoBean
    AppRunner appRunner;
    @Autowired
    PaymentService paymentService;

    @Autowired
    OrderRepository orderRepo;
    @Autowired
    ProductRepository productRepo;
    @Autowired
    CustomerService customerService;

    @AfterEach
    void cleanup(){

    }

    Product testProduct;
    Order testOrder;
    Payment testPayment;
    Customer testCustomer;

    @BeforeEach
    void setup(){
        testCustomer = customerService.registerCustomer("NewCustomer","Email22@Live.se");
        testProduct = productRepo.save(new Product("Test","testprod", BigDecimal.valueOf(2)));
        Order neworder = new Order();
        neworder.setCustomer(testCustomer);
        neworder.addOrderItem(testProduct,1);
        testOrder = orderRepo.save(neworder);
        testPayment = paymentService.savePayment(PaymentMethod.INVOICE, PaymentStatus.PENDING,testOrder);
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
        assertEquals(PaymentStatus.APPROVED,paymentService.findByID(newPayment.getId()).get().getStatus());
    }


    @Test
    @Transactional
    void FailedPaymentResultInDeclinedStatusPayment() {

        Order failingPaymentOrder = new Order();
        failingPaymentOrder.setCustomer(testCustomer);
        failingPaymentOrder.addOrderItem(testProduct,1);
        failingPaymentOrder.setTotal_Price(failingPaymentOrder.calculatePriceOfProducts());
        Order order = orderRepo.save(failingPaymentOrder);


        assertThrows(RuntimeException.class,()->paymentService.cardPayFailure(order));
        assertTrue(paymentService.paymentWithStatusExistsForOrderID(order.getId(), PaymentStatus.DECLINED));
    }
}