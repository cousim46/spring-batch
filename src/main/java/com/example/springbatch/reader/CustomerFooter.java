package com.example.springbatch.reader;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.batch.item.file.FlatFileFooterCallback;

public class CustomerFooter implements FlatFileFooterCallback {
    private ConcurrentHashMap<String, Integer> aggregateCustomers;

    public CustomerFooter(ConcurrentHashMap<String, Integer> aggregateCustomers) {
        this.aggregateCustomers = aggregateCustomers;
    }

    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write(String.format("총 고객 수 : %d", aggregateCustomers.get("TOTAL_CUSTOMERS")));
        writer.write(System.lineSeparator());
        writer.write(String.format("총 나이 : %d", aggregateCustomers.get("TOTAL_AGES")));
    }
}
