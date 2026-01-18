package org.chasdb.gruppuppgift.models.dto;

import org.chasdb.gruppuppgift.models.Product;

public record TopProductDTO(Product product, Long totalSold) {}
