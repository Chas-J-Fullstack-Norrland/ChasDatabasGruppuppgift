package org.chasdb.gruppuppgift.services;


import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Payment;
import org.chasdb.gruppuppgift.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;

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

    @Transactional
    public Payment cardPay(Order o){ //Used to pay off an order upon checkout.


        if(repo.existsByOrderIdAndStatus(o.getId(),"APPROVED")){
            throw new IllegalArgumentException("Order already paid");
        }

        if(repo.existsByOrderIdAndStatus(o.getId(),"PENDING")){
            List<Payment> ExistingPayments = repo.findAllByOrderId(o.getId());
            ExistingPayments.forEach(payment -> {
                if(payment.getStatus().equals("PENDING")){
                    payment.setStatus("CANCELLED");
                }
            });
        }

        Payment payment = new Payment("CARD","PENDING",o);

        Random r = new Random();

        try{
            if(r.nextInt(10)<9) { //10% of failure
                payment.setStatus("APPROVED");
                return payment = repo.save(payment);
            } else {
                throw new RuntimeException("Something went wrong, Try again");
            }
        }catch (Exception e){
            failedPayment(payment);
            throw e;
        }



    }

    @Transactional
    public Payment cardPayFailure(Order o){ //Used to pay off an order upon checkout.


        if(repo.existsByOrderIdAndStatus(o.getId(),"APPROVED")){
            throw new IllegalArgumentException("Order already paid");
        }

        if(repo.existsByOrderIdAndStatus(o.getId(),"PENDING")){
            List<Payment> ExistingPayments = repo.findAllByOrderId(o.getId());
            ExistingPayments.forEach(payment -> {
                if(payment.getStatus().equals("PENDING")){
                    payment.setStatus("CANCELLED");
                }
            });
        }

        Payment payment = new Payment("CARD","PENDING",o);

        Random r = new Random();


        if(r.nextInt(10)<0) { //10% of failure
            payment.setStatus("APPROVED");
            return payment = repo.save(payment);
        } else {
            failedPayment(payment);
            throw new RuntimeException("Something went wrong, Try again");
        }


    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failedPayment(Payment p){
        p.setStatus("DECLINED");
        repo.save(p);
    }

    public Boolean paymentWithStatusExistsForOrderID(Long orderID,String status){
        return repo.existsByOrderIdAndStatus(orderID, status);
    }


    public Optional<Payment> findByID(Long id){
        return repo.findById(id);
    }
    public Payment savePayment(String method, String status, Order order){
        return repo.save(new Payment(method, status, order));
    }

    public void deletePayment(Payment p){
        repo.delete(p);
    }


}
