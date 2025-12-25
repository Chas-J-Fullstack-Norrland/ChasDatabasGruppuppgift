package org.chasdb.gruppuppgift.models.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueDTO(LocalDate date, BigDecimal totalRevenue) {}
