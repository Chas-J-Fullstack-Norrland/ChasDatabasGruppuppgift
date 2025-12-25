package org.chasdb.gruppuppgift.services;

import org.chasdb.gruppuppgift.models.Inventory;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.repositories.InventoryRepository;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final CartServiceContract cartServiceContract;
    private final ReservationService reservationService;

    public OrderService(ProductRepository productRepository,
                        OrderRepository orderRepository,
                        InventoryRepository inventoryRepository,
                        CartServiceContract cartServiceContract,
                        ReservationService reservationService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.cartServiceContract = cartServiceContract;
        this.reservationService = reservationService;
    }

    @Transactional
    public Order processCheckout(String customerEmail, PaymentMethod paymentMethod) {
        CartServiceContract.Cart cart = cartServiceContract.getCart(customerEmail);

        if (cart.items().isEmpty()) {
            throw new IllegalStateException("Kundvagnen är tom. Kan inte checka ut.");
        }

        if (!simulatePayment()) {
            throw new IllegalStateException("Betalning nekad (test). Försök igen.");
        }

        Order order = new Order();
        order.setCustomer(cart.customer());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(paymentMethod);

        order.setTotalPrice(BigDecimal.valueOf(cart.getTotalPrice()));

        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartServiceContract.CartItem cartItem : cart.items()) {
            Product product = cartItem.product();

            OrderItem orderItem = new OrderItem(order, product, cartItem.quantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            orderItems.add(orderItem);

            Inventory inventory = inventoryRepository.findByProduct(product)
                    .orElseThrow(() -> new IllegalStateException("Lagersaldo saknas för produkt: " + product.getSku()));

            if (inventory.getQuantity() < cartItem.quantity()) {
                throw new IllegalStateException("Kritiskt fel: Lagersaldot är lägre än reservationen för " + product.getName());
            }

            inventory.setQuantity(inventory.getQuantity() - cartItem.quantity());
            inventoryRepository.save(inventory);

            if (cartItem.reservationId() != null) {
                reservationService.releaseReservationByID(cartItem.reservationId());
            }
        }

        order.setItems(orderItems);
        return orderRepository.save(order);
    }

    public Order createOrder(Order newOrder) {
        return orderRepository.save(newOrder);
    }

    @Transactional
    public Order addItemToOrder(Long orderId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));


        OrderItem item = new OrderItem(order, product, quantity);
        item.setPriceAtPurchase(product.getPrice());

        order.getItems().add(item);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        return order.getTotalPrice();
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order hittades inte: " + id));
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

        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProduct_Sku(item.getProduct().getSku())
                    .orElseThrow(() -> new IllegalStateException("Lager saknas för produkt: " + item.getProduct().getSku()));

            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
        }

        orderRepository.delete(order);
    }
}
