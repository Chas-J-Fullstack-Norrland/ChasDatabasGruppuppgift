package org.chasdb.gruppuppgift.cli.handlers;

import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.models.Order;
import org.chasdb.gruppuppgift.models.OrderItem;
import org.chasdb.gruppuppgift.models.Payment;
import org.chasdb.gruppuppgift.models.enums.OrderStatus;
import org.chasdb.gruppuppgift.models.enums.PaymentMethod;
import org.chasdb.gruppuppgift.services.OrderService;
import org.chasdb.gruppuppgift.services.PaymentService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCommandHandler implements CommandHandler {

    private final OrderService orderService;
    private final PaymentService paymentService;

    private String activeCustomerEmail = null;

    public OrderCommandHandler(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @Override
    public String getDomain() {
        return "order";
    }


    @Override
    public void handle(CommandInput input) {
        try {

            switch (input.action()) {
                case "checkout" -> handleCheckout(input);
                case "list" -> handleList(input);
                case "show" -> handleShow(input);
                case "select" -> handleSelect(input);
                case "cancel" -> handleCancel(input);
                default -> System.out.println("Okänd order-åtgärd.");
            }
        } catch (Exception e) {
            System.out.println("ORDER FEL: " + e.getMessage());
        }
    }

    private void handleSelect(CommandInput input) {
        String email = input.flags().get("customer");
        if (email != null) {
            this.activeCustomerEmail = email;
            System.out.println("Order-system: Kund vald (" + email + ")");
        } else {
            System.out.println("Ange --customer=<email>");
        }
    }

    private void handleCheckout(CommandInput input) {
        String email = input.flags().getOrDefault("customer", activeCustomerEmail);

        if (email == null) {
            System.out.println("Fel: Ingen kund identifierad. Använd --customer eller 'order select'.");
            return;
        }

        String methodStr = input.flags().get("method");
        if (methodStr == null) {
            System.out.println("Fel: Ange betalmetod med --method=CARD eller --method=INVOICE");
            return;
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Fel: Ogiltig metod. Tillåtna: CARD, INVOICE");
            return;
        }

        System.out.println("Påbörjar checkout för " + email + " med " + method + "...");

        try {
            Order order = orderService.processCheckout(email, method);

            System.out.println("BESTÄLLNING LAGD!");
            System.out.println("Order ID: " + order.getId());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Totalt: " + order.getTotal_Price() + " kr");
            System.out.println("Kvitto skickat till " + email); // Simulering

        } catch (IllegalStateException e) {
            System.out.println("CHECKOUT MISSLYCKADES: " + e.getMessage());
        }
    }

    private void handleCancel(CommandInput input) {
        String idStr = input.args().getFirst();
        if (idStr.isEmpty()) {
            System.out.println("Ange Order ID för att makulera. T.ex: order cancel 5");
            return;
        }

        try {
            Long orderId = Long.parseLong(idStr);
            System.out.println("Makulerar order " + orderId + "...");

            orderService.cancelOrder(orderId);

            System.out.println("Order " + orderId + " har makulerats och lagret har återställts.");

        } catch (NumberFormatException e) {
            System.out.println("Order ID måste vara en siffra.");
        } catch (IllegalArgumentException e) {
            System.out.println("Kunde inte hitta ordern: " + e.getMessage());
        }
    }

    private void handleList(CommandInput input) {
        List<Order> orders;
        String statusStr = input.flags().get("status");

        if (statusStr != null) {
            try {
                OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
                orders = orderService.listOrdersByStatus(status);
            } catch (IllegalArgumentException e) {
                System.out.println("Ogiltig status.");
                return;
            }
        } else {
            orders = orderService.findAll();
        }

        System.out.printf("%-5s %-20s %-10s %-10s %-15s%n", "ID", "Datum", "Summa", "Status", "Kund");
        System.out.println("----------------------------------------------------------------");
        for (Order o : orders) {
            System.out.printf("%-5d %-20s %-10s %-10s %-15s%n",
                    o.getId(),
                    o.getCreatedAt().toString().substring(0, 16),
                    o.getTotal_Price(),
                    o.getStatus(),
                    o.getCustomer().getEmail());
        }
    }

    private void handleShow(CommandInput input) {
        String idStr = input.args().getFirst();
        if (idStr.isEmpty()) {
            System.out.println("Ange Order ID.");
            return;
        }

        try {
            Long id = Long.parseLong(idStr);
            Order order = orderService.findOrderById(id);

            System.out.println("\n--- ORDER DETALJER: " + id + " ---");
            System.out.println("Kund: " + order.getCustomer().getName() + " (" + order.getCustomer().getEmail() + ")");
            System.out.println("Datum: " + order.getCreatedAt());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Betalningar för "+order.getOrdercode());
            paymentService.paymentsForOrder(order.getId()).forEach(
                    payment-> System.out.println(" "+payment.getReference()+": "+payment.getMethod().toString())
            );
            System.out.println("\nProdukter:");

            for (OrderItem item : order.getItems().values()) {
                System.out.printf("- %dx %s (á %s kr)%n",
                        item.getQuantity(),
                        item.getProduct().getName(),
                        item.getUnitPrice());
            }
            System.out.println("\nTOTALT: " + order.getTotal_Price() + " kr");

        } catch (NumberFormatException e) {
            System.out.println("ID måste vara en siffra.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}