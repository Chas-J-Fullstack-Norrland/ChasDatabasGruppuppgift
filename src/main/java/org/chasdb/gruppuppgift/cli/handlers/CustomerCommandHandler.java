package org.chasdb.gruppuppgift.cli.handlers;

import org.chasdb.gruppuppgift.cli.CommandHandler;
import org.chasdb.gruppuppgift.cli.CommandInput;
import org.chasdb.gruppuppgift.models.Customer;
import org.chasdb.gruppuppgift.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomerCommandHandler implements CommandHandler {

    @Autowired
    private CustomerService service;

    @Override
    public void handle(CommandInput input) {
        try {
            switch (input.action()) {
                case "add" -> add(input);
                case "list" -> list();
                default -> System.out.println("Okänd åtgärd för kategori.");
            }
        } catch (Exception e) {
            System.out.println("FEL: " + e.getMessage());
        }
    }

    @Override
    public String getDomain() {
        return "customer";
    }

    private void add(CommandInput input){
        if(!input.flags().containsKey("name")){
            System.err.println("Customer require a name, include one with --name=");
            return;
        }
        if(!input.flags().containsKey("email")){
            System.err.println("Customer require an email, include one with --email=");
            return;
        }
        service.registerCustomer(input.flags().get("name"),input.flags().get("email"));
    }
    private void list(){
        System.out.println("!Registered Customers!");
        service.listCustomers().forEach(System.out::println);
    }
    private void find(CommandInput input){
        if(!input.flags().containsKey("email")){
            System.err.println("Command requires an email to search for, include one with --email=");
            return;
        }

        try {
            Customer c = service.getCustomerByEmail(input.flags().get("email")).orElseThrow();
            System.out.println(c.printString());
        } catch (Exception e){
            System.err.println("Could not find customer with email");

        }
    }
}
