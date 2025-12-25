package org.chasdb.gruppuppgift.services;
import org.chasdb.gruppuppgift.models.Product;

public record ProductSalesDTO(
    Product product,
    int quantitySold
) {}
