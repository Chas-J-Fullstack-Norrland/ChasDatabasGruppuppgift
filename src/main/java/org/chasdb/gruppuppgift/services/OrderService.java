package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.*;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.models.enums.PaymentStatus;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;

import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.PaymentRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    private final OrderNumberGenerator codeGenerator;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final CartServiceContract cartServiceContract;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final CustomerService customerService;

    public OrderService(
            OrderNumberGenerator orderNumberGenerator,
            CustomerService customerService,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            InventoryRepository inventoryRepository,
            CartServiceContract cartServiceContract,
            ReservationService reservationService,
            PaymentService paymentService
    ) {
        this.codeGenerator = orderNumberGenerator;
        this.customerService = customerService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.cartServiceContract = cartServiceContract;
        this.reservationService = reservationService;
        this.paymentService = paymentService;
    }

    @Transactional
    public Order processCheckout(String customerEmail, PaymentMethod paymentMethod) { //TODO! REWRITE!
        CartServiceContract.Cart cart = cartServiceContract.getCart(customerEmail);

        if (cart.items().isEmpty()) {
            throw new IllegalStateException("Kundvagnen är tom. Kan inte checka ut.");
        }

        Order order = new Order(cart.customer(),LocalDateTime.now(),OrderStatus.PENDING);
        order.setOrdercode(codeGenerator.next());

        order.setTotal_Price(BigDecimal.valueOf(cart.getTotalPrice()));

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartServiceContract.CartItem cartItem : cart.items()) {
            Product product = cartItem.product();
            OrderItem orderItem = new OrderItem(order, product, cartItem.quantity());
            orderItems.add(orderItem);
        }


        order.setItems(orderItems);
        orderRepository.save(order);

        Payment p = new Payment();
        int attempts = 0;
        while(attempts<4) {
            try {
                switch (paymentMethod) {
                    case PaymentMethod.CARD -> p = paymentService.cardPay(order);
                    case PaymentMethod.INVOICE -> p = paymentService.savePayment(PaymentMethod.INVOICE, PaymentStatus.PENDING, order);
                    default -> throw new IllegalArgumentException("Invalid payment method");
                }
                attempts = 4; //Pass and do not repeat
            } catch (RuntimeException e) {
                attempts++;

            }
        }

        switch (p.getStatus()){
            case PaymentStatus.APPROVED -> order.setStatus(OrderStatus.PAID);
        }


        reservationService.deleteReservationByCustomerId(cart.customer().getId());
        return order;
    }
        /**
     * Skapar en tom order (items läggs till efteråt)
     */

    public Order createOrder(Order newOrder) {
        newOrder.setOrdercode(codeGenerator.next());
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
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        OrderItem item = new OrderItem( //Constructor saves the current price of product as UnitPrice
                order,
                product,
                quantity
        );
        order.getItems().put(item.getProduct().getSku(),item);

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
    public Order checkout(Customer c, List<Product> products, String paymentMethod){ //Purely used for testing the order creation -> payment procedure.

        customerService.findCustomerByID(c.getId()).orElseThrow(()->new NoSuchElementException("Customer does not exist in DB"));

        Order newOrder = new Order();
        newOrder.setOrdercode(codeGenerator.next());
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
                    case "INVOICE" -> p = paymentService.savePayment(PaymentMethod.INVOICE, PaymentStatus.PENDING, savedOrder);
                    default -> throw new IllegalArgumentException("Invalid payment method");
                }
                attempts = 4; //Pass and do not repeat
            } catch (RuntimeException e) {
                attempts++;

            }
        }

        switch (p.getStatus()){
            case PaymentStatus.APPROVED -> savedOrder.setStatus(OrderStatus.PAID);
        }


        reservationService.deleteReservationByCustomerId(c.getId());
        return newOrder;

    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order hittades inte: " + id));
    }
    public Order findOrderByCode(String code){
        return orderRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("No order with code "+code));
    }

    public List<Order> listOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    private boolean simulatePayment() {
        return new Random().nextDouble() < 0.90;
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order hittades inte med ID: " + orderId));

        for (OrderItem item : order.getItems().values()) {
            Inventory inventory = inventoryRepository.findByProduct_Sku(item.getProduct().getSku()) //Unnecceary check,  If product was persisted then inventory exists guaranteed.
                    .orElseThrow(() -> new IllegalStateException("Lager saknas för produkt: " + item.getProduct().getSku()));

            inventory.setQty(inventory.getQty() + item.getQuantity());
            inventoryRepository.save(inventory);
        }

        orderRepository.delete(order);
    }
}
