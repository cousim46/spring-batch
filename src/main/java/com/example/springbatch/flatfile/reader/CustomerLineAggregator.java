package com.example.springbatch.flatfile.reader;

import org.springframework.batch.item.file.transform.LineAggregator;

public class CustomerLineAggregator implements LineAggregator<Customer> {

    @Override
    public String aggregate(Customer item) {
        return item.getName() + "," + item.getAge();
    }
}
