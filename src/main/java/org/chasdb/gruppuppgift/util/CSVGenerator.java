package org.chasdb.gruppuppgift.util;

import org.apache.commons.text.RandomStringGenerator;
import org.chasdb.gruppuppgift.models.Payment;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;

public class CSVGenerator {

    static Random random = new Random();
    static DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Map<String,Integer> amountToGenerate;

    public void setVariant(int variant){
        if (amountToGenerate==null){
            amountToGenerate = new HashMap<>();
        }
        amountToGenerate.values().forEach(v-> v=0);

        switch (variant){
            case 1 ->{
                amountToGenerate.put("CUSTOMER",5);
                amountToGenerate.put("PRODUCT",10);
                amountToGenerate.put("ORDERS",0);
            }
            case 2 ->{
                amountToGenerate.put("CUSTOMER",100);
                amountToGenerate.put("PRODUCT",50);
                amountToGenerate.put("ORDERS",50);

            }
            case 3 ->{
                amountToGenerate.put("CUSTOMER",500);
                amountToGenerate.put("PRODUCT",1000);
                amountToGenerate.put("ORDERS",2000);
            }
            default -> {
                amountToGenerate.put("CUSTOMER",5000);
                amountToGenerate.put("PRODUCT",10000);
                amountToGenerate.put("ORDERS",200000);
            }

        }
    }

    public void GenerateCSV(String filename) throws IOException {

        if(amountToGenerate==null){
            setVariant(3);
        }

        RandomStringGenerator generator =
                new RandomStringGenerator.Builder()
                        .withinRange('0', 'z')
                        .filteredBy(Character::isLetterOrDigit)
                        .build();




        try (FileWriter writer = new FileWriter(filename+".csv")) {
            // header
            writer.write("type,name,email,createdAt,sku,description,price,active,qty,category,order,items,status,method,reference,expiresAt\n");

            // 1. Categories
            String[] categories = {"Shirts","Pants","Shoes","Hats","Jackets","Accessories","Socks","Outerwear","Underwear","Sportswear"};
            for (String cat : categories) {
                writer.write(String.format("CATEGORY,%s,,,,,,,,,,,,,,,\n", cat));
            }

            // 2. Customers
            for (int i = 1; i <= amountToGenerate.get("CUSTOMER"); i++) {
                String name = "Customer " + i;
                String email = "customer" + i + "@example.com";
                String createdAt = LocalDateTime.now().minusDays(random.nextInt(365)).format(dtf);
                writer.write(String.format("CUSTOMER,%s,%s,%s,,,,,,,,,,,,,\n", name, email, createdAt));
            }

            // 3. Products
            for (int i = 1; i <= amountToGenerate.get("PRODUCT"); i++) {
                String name = "Product " + i;
                String sku = "SKU" + i;
                String desc = "Description for " + name;
                double price = 5 + (500 - 5) * random.nextDouble(); // price 5-500
                boolean active = random.nextBoolean();
                int qty = 1 + random.nextInt(100);
                // pick 1-3 random categories
                List<String> catList = new ArrayList<>();
                Collections.shuffle(Arrays.asList(categories));
                for (int j = 0; j < 1 + random.nextInt(3); j++) {
                    catList.add(categories[j]);
                }
                String catString = String.join("|", catList);
                String createdAt = LocalDate.now().minusDays(random.nextInt(365)).format(DateTimeFormatter.ISO_LOCAL_DATE);
                writer.write(String.format(
                        "PRODUCT,%s,,%s,%s,%s,%f,%b,%d,%s,,,,,\n",
                        name, createdAt, sku, desc, price, active, qty, catString));
            }

            if(amountToGenerate.get("ORDERS")>0){

            // 4. Orders
            for (int i = 1; i <= amountToGenerate.get("ORDERS"); i++) {
                String order = "ORD-" + String.format("%05d", i);
                int custId = 1 + random.nextInt(amountToGenerate.get("CUSTOMER"));
                String customerEmail = "customer" + custId + "@example.com";

                int itemCount = 1 + random.nextInt(5);
                List<String> items = new ArrayList<>();
                List<String> quantities = new ArrayList<>();

                for (int j = 0; j < itemCount; j++) {
                    int prodId = 1 + random.nextInt(amountToGenerate.get("PRODUCT"));
                    items.add("SKU" + prodId);
                    quantities.add(String.valueOf(1 + random.nextInt(5)));
                }

                String itemsStr = String.join("|", items);
                String qtyStr = String.join("|", quantities);

                double totalPrice = itemCount * (5 + (500 - 5) * random.nextDouble());
                String createdAt = LocalDateTime.now()
                        .minusDays(random.nextInt(365))
                        .format(dtf);

                String[] statuses = {"NEW", "PAID", "PENDING", "CANCELLED","SHIPPED"};
                String status = statuses[random.nextInt(statuses.length)];

                writer.write(String.format(
                        "ORDER,,%s,%s,,,%f,,%s,,%s,%s,%s,,,\n",
                        customerEmail,
                        createdAt,
                        totalPrice,
                        qtyStr,
                        order,
                        itemsStr,
                        status
                ));
            }

            // 5. Payments
            String[] methods = {"CARD","INVOICE"};
            String[] payStatuses = {"APPROVED","DECLINED","PENDING"};
            for (int i = 1; i <= amountToGenerate.get("ORDERS"); i++) {
                String orderCode = "ORD-" + String.format("%05d", i);
                String method = methods[random.nextInt(methods.length)];
                String status = payStatuses[random.nextInt(payStatuses.length)];
                String createdAt = LocalDateTime.now().minusDays(random.nextInt(365)).format(dtf);
                String reference = method + "-" + generator.generate(10);
                writer.write(String.format("PAYMENT,,,%s,,,,,,,%s,,%s,%s,%s,\n",
                        createdAt,orderCode, status, method, reference ));
            }
            }

            // 6. Reservations
            int limit = random.nextInt(amountToGenerate.get("CUSTOMER"))*5;
            for (int i = 1; i <= limit; i++) {
                int custId = 1 + random.nextInt(amountToGenerate.get("CUSTOMER"));
                int prodId = 1 + random.nextInt(amountToGenerate.get("PRODUCT"));
                String email = "customer" + custId + "@example.com";
                String sku = "SKU" + prodId;
                int qty = 1 + random.nextInt(5);
                Instant createdAt = Instant.now().minus(random.nextInt(365), ChronoUnit.DAYS);
                Instant expiresAt = Instant.now().plus(15 + random.nextInt(120),ChronoUnit.MINUTES);
                writer.write(String.format("RESERVATION,,%s,%s,%s,,,,%d,,,,,,,%s\n", email,createdAt,sku, qty , expiresAt));
            }

        }

        System.out.println("CSV FILE "+ filename  +" generated successfully!");
    }
}