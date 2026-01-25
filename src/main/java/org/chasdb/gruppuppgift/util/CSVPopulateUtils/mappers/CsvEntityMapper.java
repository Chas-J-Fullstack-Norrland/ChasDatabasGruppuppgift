package org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers;

import org.apache.commons.csv.CSVRecord;

public interface CsvEntityMapper<T> {
    T map(CSVRecord record);
    void save(T entity);

    void init();
    String supportsType();
}