package com.example.springbatch.config;

import com.example.springbatch.task.GreetingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BasicTaskJobConfiguration {
    @Autowired
    private final PlatformTransactionManager transactionManager;

    /**
     * {@link com.example.springbatch.task.GreetingTask}
     * */
    @Bean
    public Tasklet greetingTasklet() {
        return new GreetingTask();
    }

    /**
     * JobRepository와 PlatformTransactionManager를 파라미터로 받음.
     * 스프링 배치는 보통 데이터 소스와 함께 작업하므로 PlatformTransactionManager이 필요.
     * StepBuild를 통해 생성하고 myStep으로 이름을 지정하고 해당 스텝을 JobRepository에 등록.
     * Tasklet을 스텝에 추가하고 빈으로 등록한 greetingTasklet을 통해 Tasklet을 스탭에 주입.
     * */
    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("------------------- init myStep----------------");
        return new StepBuilder("myStep", jobRepository)
            .tasklet(greetingTasklet(), transactionManager)
            .build();
    }

    /**
     * Job은 Step과 JobRepository를 매개변수로 받음.
     * JobRepository에 Job을 등록.
     * JobBuilder를 통해 Job 생성, myJob으로 이름 등록
     * incrementer은 Job이 지속적으로 실행될때 잡의 유니크성을 구분할 수 있는 방법을 설정
     * - RunIdIncrementer는 Job 아이디를 실행할때 지속적으로 증가시키면서 유니크한 잡을 실행.
     * start(step)을 통해 Job의 시작포인트를 지정.
     * */

    @Bean
    public Job myJob(Step step, JobRepository jobRepository) {
        log.info("------------------- init myJob----------------");
        return new JobBuilder("myJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(step)
            .build();
    }

}
