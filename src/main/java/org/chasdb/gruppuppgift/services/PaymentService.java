package org.chasdb.gruppuppgift.services;


import jakarta.transaction.Transactional;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Payment;
import org.chasdb.gruppuppgift.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repo;

    @Transactional
    public Payment invoicePay(Long paymentID){ //Used to pay pending invoices, Might not implement

        Payment payment = repo.findById(paymentID).orElseThrow(()->new NoSuchElementException("No Payment with ID "+paymentID));

        if(Objects.equals(payment.getMethod(), "CARD")){
            throw new IllegalStateException("Card Payments cannot be paid through invoice");
        }
        if(Objects.equals(payment.getStatus(), "APPROVED")){
            throw new IllegalStateException("Payment has already been completed");
        }
        if(Objects.equals(payment.getStatus(), "DECLINED")){
            throw new IllegalStateException("payment has previously been declined");
        }

        Random r = new Random();

        if(r.nextInt(10)<9) { //10% of failure
            payment.setStatus("APPROVED");
            return payment;
        } else {
            throw new RuntimeException("Something went wrong, Try again");
        }


    }

    public Optional<Payment> findByID(Long id){
        return repo.findById(id);
    }
    public Payment savePayment(String method, String status, Order order){
        return repo.save(new Payment(method, status, order));
    }


}
