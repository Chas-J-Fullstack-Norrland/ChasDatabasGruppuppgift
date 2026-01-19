package org.chasdb.gruppuppgift.cli.handlers;

import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.services.CartServiceContract;
import org.springframework.stereotype.Component;

@Component
public class CartCommandHandler implements CommandHandler {

    private final CartServiceContract cartService;

    private String activeCustomerEmail = null;

    public CartCommandHandler(CartServiceContract cartService) {
        this.cartService = cartService;
    }

    @Override
    public String getDomain() {
        return "cart";
    }

    @Override
    public void handle(CommandInput input) {
        try {
            switch (input.action()) {
                case "select" -> handleSelect(input);
                case "create" -> handleCreate(input);
                case "add" -> handleAdd(input);
                case "remove" -> handleRemove(input);
                case "show" -> handleShow(input);
                case "status" -> printStatus();
                default -> System.out.println("Okänd åtgärd för kundvagn.");
            }
        } catch (Exception e) {
            System.out.println("FEL: " + e.getMessage());
        }
    }

    private void handleSelect(CommandInput input) {
        String email = input.flags().get("customer");
        if (email == null) {
            System.out.println("Fel: Du måste ange --customer=<email>");
            return;
        }

        try {
            cartService.getCart(email);
            this.activeCustomerEmail = email;
            System.out.println("Kund vald: " + email);
        } catch (IllegalArgumentException e) {
            System.out.println("Kunden hittades inte. Skapa kunden först via 'customer add'.");
        }
    }

    private void handleCreate(CommandInput input) {
        String email = input.flags().getOrDefault("customer", activeCustomerEmail);

        if (email == null) {
            System.out.println("Fel: Ingen kund angiven. Använd --customer eller 'cart select'.");
            return;
        }

        cartService.createCart(email);
        this.activeCustomerEmail = email;
        System.out.println("Kundvagn initierad för " + email);
    }

    private void handleAdd(CommandInput input) {
        if (activeCustomerEmail == null) {
            System.out.println("Fel: Ingen kund vald. Kör 'cart select' först.");
            return;
        }

        if (input.args().size() < 2) {
            System.out.println("Användning: cart add <sku> <qty>");
            return;
        }

        String sku = input.args().get(0);
        try {
            int qty = Integer.parseInt(input.args().get(1));

            cartService.addToCart(activeCustomerEmail, sku, qty);
            System.out.println("Lade till " + qty + " st " + sku + " i vagnen.");

        } catch (NumberFormatException e) {
            System.out.println("Fel: Antal måste vara en siffra.");
        }
    }

    private void handleRemove(CommandInput input) {
        if (activeCustomerEmail == null) {
            System.out.println("Fel: Ingen kund vald.");
            return;
        }

        String sku = input.args().getFirst();
        if (sku.isEmpty()) {
            System.out.println("Ange SKU att ta bort.");
            return;
        }

        cartService.removeFromCart(activeCustomerEmail, sku);
        System.out.println("Tog bort " + sku + ".");
    }

    private void handleShow(CommandInput input) {
        if (activeCustomerEmail == null) {
            System.out.println("Inget att visa (ingen kund vald).");
            return;
        }

        CartServiceContract.Cart cart = cartService.getCart(activeCustomerEmail);

        System.out.println("\n--- KUNDVAGN: " + cart.customer().getEmail() + " ---");
        if (cart.items().isEmpty()) {
            System.out.println("(Tom)");
        } else {
            System.out.printf("%-15s %-25s %-10s %-10s%n", "SKU", "Produkt", "Antal", "Styckpris");
            System.out.println("----------------------------------------------------------------");

            for (CartServiceContract.CartItem item : cart.items()) {
                System.out.printf("%-15s %-25s %-10d %-10s%n",
                        item.product().getSku(),
                        truncate(item.product().getName(), 24),
                        item.quantity(),
                        item.product().getPrice());
            }
            System.out.println("----------------------------------------------------------------");
            System.out.printf("TOTALT: %.2f kr%n", cart.getTotalPrice());
        }
    }

    private void printStatus() {
        if (activeCustomerEmail == null) {
            System.out.println("Status: Ingen kund vald.");
        } else {
            System.out.println("Status: Inloggad som " + activeCustomerEmail);
        }
    }

    private String truncate(String str, int width) {
        if (str.length() <= width) return str;
        return str.substring(0, width - 3) + "...";
    }
}