package org.chasdb.gruppuppgift.services;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.chasdb.gruppuppgift.models.Product;
import org.chasdb.gruppuppgift.repositories.OrderRepository;
import org.chasdb.gruppuppgift.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }
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
        BigDecimal priceAtOrderTime = product.getPrice();
        OrderItem item = new OrderItem(
                order,
                product,
                quantity,
                priceAtOrderTime
        );
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
        return order.getTotalPrice();
    }
}
