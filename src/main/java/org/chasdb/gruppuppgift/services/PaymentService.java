package org.chasdb.gruppuppgift.services;


import org.springframework.transaction.annotation.Transactional;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.Payment;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.models.enums.PaymentStatus;
import org.chasdb.gruppuppgift.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment invoicePay(Long paymentID) {
        Payment payment = paymentRepository.findById(paymentID)
                .orElseThrow(() -> new NoSuchElementException("No Payment with ID " + paymentID));

        if (payment.getMethod() == PaymentMethod.CARD) {
            throw new IllegalStateException("Card Payments cannot be paid through invoice");
        }
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new IllegalStateException("Payment has already been completed");
        }
        if (payment.getStatus() == PaymentStatus.DECLINED) {
            throw new IllegalStateException("Payment has previously been declined");
        }

        Random r = new Random();

        // 90% chans att lyckas
        if (r.nextInt(10) < 9) {
            payment.setStatus(PaymentStatus.APPROVED);

            return paymentRepository.save(payment);
        } else {
            throw new RuntimeException("Something went wrong, Try again");
        }
    }

    public Optional<Payment> findByID(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment savePayment(PaymentMethod method, PaymentStatus status, Order order) {
        return paymentRepository.save(new Payment(method, status, order));
    }
}
