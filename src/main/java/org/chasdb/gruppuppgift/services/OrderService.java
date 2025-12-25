package org.chasdb.gruppuppgift.services;
import org.chasdb.gruppuppgift.models.*;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.PaymentRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ReservationService reservationService;

    /**
     * Skapar en tom order (items läggs till efteråt)
     */
    public Order createOrder(Order newOrder) {
        return orderRepository.save(newOrder);
    }
    /**
     * Lägg till produkt i order
     */
    @Transactional
    public Order addItemToOrder(
            Long orderId,
            Long productId,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (product.getQTY() < quantity) {
            throw new IllegalArgumentException("Product not in stock");
        }
        OrderItem item = new OrderItem(order, product, quantity);
        order.getItems().add(item);
        return orderRepository.save(order);

    }
    /**
     * Totalpris (exakt)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return order.calculatePriceOfProducts();
    }

    @Transactional
    public Order checkout(Customer c, List<Product> products, String paymentMethod){ //Chance Customer and Productlist to instead use the contents of the cart.

        customerService.findCustomerByID(c.getId()).orElseThrow(()->new NoSuchElementException("Customer does not exist in DB"));

        Order newOrder = new Order();
        newOrder.setCustomer(c);
        products.forEach(p-> newOrder.addOrderItem(p,1));
        newOrder.setTotal_Price(newOrder.calculatePriceOfProducts());
        Order savedOrder = orderRepository.save(newOrder);

        Payment p = new Payment();
        int attempts = 0;
        while(attempts<4) {
            try {
                switch (paymentMethod) {
                    case "CARD" -> p = paymentService.cardPay(savedOrder);
                    case "INVOICE" -> p = paymentService.savePayment("INVOICE", "PENDING", savedOrder);
                    default -> throw new IllegalArgumentException("Invalid payment method");
                }
                attempts = 4; //Pass and do not repeat
            } catch (RuntimeException e) {
                attempts++;

            }
        }

        switch (p.getStatus()){
            case "APPROVED" -> savedOrder.setStatus("PAID");
        }


        reservationService.deleteReservationByCustomerId(c.getId());
        return newOrder;

    }




}
