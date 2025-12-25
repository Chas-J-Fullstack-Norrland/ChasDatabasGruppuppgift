package org.chasdb.gruppuppgift.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class CSVImporter {

    private static final Logger log = LoggerFactory.getLogger(CSVImporter.class);
    private static final int DEFAULT_BATCH_SIZE = 1000;

    public record ImportResult(int successCount, int failureCount, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    public <T> ImportResult importData(Reader reader,
                                       Function<CSVRecord, T> mapper,
                                       Consumer<List<T>> batchSaver) {
        return importData(reader, mapper, batchSaver, DEFAULT_BATCH_SIZE);
    }

    public <T> ImportResult importData(Reader reader,
                                       Function<CSVRecord, T> mapper,
                                       Consumer<List<T>> batchSaver,
                                       int batchSize) {

        List<T> batch = new ArrayList<>(batchSize);
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        CSVFormat format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        try (CSVParser csvParser = new CSVParser(reader, format)) {
            for (CSVRecord csvRecord : csvParser) {
                try {
                    T item = mapper.apply(csvRecord);

                    if (item != null) {
                        batch.add(item);
                    }

                    if (batch.size() >= batchSize) {
                        saveBatch(batch, batchSaver);
                        successCount += batch.size();
                        batch.clear();
                    }
                } catch (Exception e) {
                    failureCount++;
                    String errorMsg = "Rad %d: %s".formatted(csvRecord.getRecordNumber(), e.getMessage());
                    errors.add(errorMsg);
                    log.warn("Misslyckades att importera rad {}: {}", csvRecord.getRecordNumber(), e.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                saveBatch(batch, batchSaver);
                successCount += batch.size();
            }

        } catch (IOException e) {
            log.error("Kritiskt IO-fel vid CSV-läsning", e);
            errors.add("Kritiskt filfel: " + e.getMessage());
            return new ImportResult(successCount, failureCount, errors);
        }

        log.info("CSV Import klar. Lyckades: {}, Misslyckades: {}", successCount, failureCount);
        return new ImportResult(successCount, failureCount, errors);
    }

    private <T> void saveBatch(List<T> batch, Consumer<List<T>> batchSaver) {
        try {
            batchSaver.accept(batch);
        } catch (Exception e) {
            log.error("Kunde inte spara batch om {} objekt", batch.size(), e);
            throw new RuntimeException("Batch-sparning misslyckades", e);
        }
    }
}