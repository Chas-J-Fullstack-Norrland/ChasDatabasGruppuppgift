package org.chasdb.gruppuppgift.services;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderNumberGenerator {
    private final AtomicLong counter = new AtomicLong(0);
    LocalDateTime time = LocalDateTime.now();

    public String next() {
        return "ORD-" + time.format(DateTimeFormatter.ofPattern("uuwwddkkmm")) + "-" +
                String.format("%06d", counter.incrementAndGet());
    }

}
