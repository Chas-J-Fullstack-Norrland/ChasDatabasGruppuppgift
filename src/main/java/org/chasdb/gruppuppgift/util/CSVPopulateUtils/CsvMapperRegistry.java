package org.chasdb.gruppuppgift.util.CSVPopulateUtils;

import org.chasdb.gruppuppgift.util.CSVPopulateUtils.mappers.CsvEntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CsvMapperRegistry {

    private final Map<String, CsvEntityMapper<?>> mappers;

    public CsvMapperRegistry(List<CsvEntityMapper<?>> mapperList) {
        this.mappers = mapperList.stream()
                .collect(Collectors.toMap(
                        m -> m.supportsType().toUpperCase(),
                        Function.identity()
                ));
    }

    @SuppressWarnings("unchecked")
    public <T> CsvEntityMapper<T> getMapper(String type) {
        return (CsvEntityMapper<T>) mappers.get(type.toUpperCase());
    }
}