package com.example.springbatch.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
public class GreetingTask implements Tasklet, InitializingBean {

    /**
     * RepeatStatus
     * - FINISHED : 태스크릿이 종료 된 샅애
     * - CONTINUABLE : 계속해서 테스크 수행 하는 상태
     * - continueIf(condition) : 조건에 따라 종료할지 지속할지 결정하는 메서드.
     * */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception {
        log.info("--------------------Task Execute------------------");
        log.info("GreetingTask {}, {}", contribution, chunkContext);
        return RepeatStatus.FINISHED;
    }

    /**
     * 태스크를 수행할 때 프로퍼티를 설정하고 난 뒤 수행.
     * */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("------------------After Properites Sets()-----------------------");
    }
}
