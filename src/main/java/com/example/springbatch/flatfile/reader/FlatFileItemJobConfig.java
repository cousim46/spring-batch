package com.example.springbatch.flatfile.reader;

import com.example.springbatch.flatfile.processor.AggregateCustomerProcessor;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class FlatFileItemJobConfig {

    public static final int CHUNK_SIZE  = 100;
    public static final String ENCODING = "UTF-8";
    public static final String FLAT_FILE_CHUNK_JOB = "FALT_FILE_CHUNK_JOB";

    private ConcurrentHashMap<String, Integer> aggregateCustomers = new ConcurrentHashMap<>();
    private final ItemProcessor<Customer, Customer> itemProcessor = new AggregateCustomerProcessor(aggregateCustomers);


    @Bean
    public FlatFileItemReader<Customer> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
            .name("FlatFileItemReader")
            .resource(new ClassPathResource("./customer.csv")) // 읽을 파일 위치
            .encoding(ENCODING) // encoding
            .delimited() //구분자로 설정되어 있음을 의미
            .delimiter(",") // 구분자 설정
            .names("name", "age", "gender") // 구분자로 구분된 데이터의 이름을 지정
            .targetType(Customer.class) // . 구분된 데이터를 모델에 넣을지 클래스 타입 지정
            .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> flatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
            .name("flatFileItemWriter")
            .resource(new FileSystemResource("./output/customer_new.csv"))
            .encoding(ENCODING)
            .delimited()
            .delimiter("\t")
            .names("Name", "Age", "Gender")
            .append(false)
            .lineAggregator(new CustomerLineAggregator())
            .headerCallback(new CustomerHeader())
            .footerCallback(new CustomerFooter(aggregateCustomers))
            .build();
    }


    @Bean
    public Step flatFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("------------------ Init flatFileStep ----------------");
        return new StepBuilder("flatFileStep", jobRepository)
            .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
            .reader(flatFileItemReader())
            .processor(itemProcessor)
            .writer(flatFileItemWriter())
            .build();
    }

    @Bean
    public Job flatFileJob(Step flatFileStep, JobRepository jobRepository) {
        return new JobBuilder(FLAT_FILE_CHUNK_JOB, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(flatFileStep)
            .build();
    }
}
