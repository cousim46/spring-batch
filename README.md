## 스프링 배치 스키마 구조

![img.png](스프링배치%20스키마%20구조.png)

### BATCH_JOB_INSTANCE Table
- 배치 잡 인스턴스 테이블
- 배치가 수행되면 Job이 생성이되고, 해당 Job 인스턴스에 대해서 관련된 모든 정보를 가진 최상위 테이블
```sql
CREATE TABLE BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT PRIMARY KEY ,
    VERSION BIGINT,
    JOB_NAME VARCHAR(100) NOT NULL,
    JOB_KEY VARCHAR(32) NOT NULL
)
```
- JOB_INSTANCE_ID : 인스턴스에 대한 유니크 아이디입니다. JobInstance 객체의 getId로 획득 가능.
- VERSION : 버전 정보
- JOB_NAME : 배치 Job 객체로 획득한 Job 이름입니다.
- JOB_KEY : JobParameter를 직렬화한 데이터 값이며, 동일한 Job을 다른 Job과 구분하는 값입니다. Job은 이 JobParameter가 동일할 수 없으며, JOB_KEY는 구별될 수 있도록 달라야합니다.

### BATCH_JOB_EXECUTION Table
- JobExecution과 관련된 모든 정보를 저장
- Job이 매번 실행될때마다 JobExecution이라는 새로운 객체가 있으며, 이 테이블에 새로운 데이터로 생성됩니다.

```sql
CREATE TABLE BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID BIGINT PRIMARY KEY ,
    VERSION BIGINT,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP DEFAULT NULL,
    END_TIME TIMESTAMP DEFAULT NULL,
    STATUS VARCHAR(10),
    EXIT_CODE VARCHAR(20),
    EXIT_MESSAGE VARCHAR(2500),
    LAST_UPDATED TIMESTAMP,
    constraint JOB_INSTANCE_EXECUTION_FK foreign key (JOB_INSTANCE_ID)
    references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)                            
)
```
- JOB_EXECUTION_ID : 배치자 실행 아이디, 실행을 유니크하게 구분할 수 있습니다. 컬럼의 값은 JobExecution의 getId 메서드로 획득이 가능합니다.
- VERSION : 버전 정보
- JOB_INSTANCE_ID : BATCH_JOB_INSTANCE 테이블의 기본키이면서 BATCH_JOB_EXECUTION 테이블에서 외래키입니다. execution이 소속된 인스턴스가 됩니다. 하나의 인스턴스에는 여러 execution이 존재할 수 있습니다.
- CREATE_TIME : execution이 생성된 시간
- START_TIME : execution이 시작된 시간
- END_TIME : execution이 종료된 시간. 성공 실패 유무 없이 이력이 남습니다. Job이 현재 실행중이 아닐때 열의 값이 비어 있다면 특정 유형의 오류가 발생하여 프레임워크가 실패하기 전 마지막 저장을 수행할 수 없음을 나타냅니다.
- STATUS : execution의 현재 상태를 문자열로 나타냅니다. COMPLETED, STARTED 및 기타 등 BatchStatus 나열값으로 채워집니다.
- EXIT_CODE : execution의 종료 코드를 문자열로 나타냅니다. 커맨드라인 잡의 케이스에서는 숫자로 변환됩니다.
- EXIT_MESSAGE : Job이 종료되는 경우  어떻게 종료되었는지 나타내고 가능하면 stack trace값이 남게됩니다.
- LAST_UPDATED : execution이 마지막으로 지속된 시간을 나타내는 타임스탬프입니다.

### BATCH_JOB_EXECUTION_PARAMS Table
- JobParameter에 대한 정보를 저장하는 테이블입니다.
- 하나 이상의 key/value 쌍으로 Job에 전달되며, Job이 실행될때 전달된 파라미터 정보를 저장하게 된다.
- 각 파라미터는 IDENTIFYING이 true로 설정되면, JobParameter 생성 시 유니크한 값으로 사용된 경우라는 의미.
- 테이블은 비정규화 형태

```sql
CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID BIGINT NOT NULL,
    PARAMETER_NAME VARCHAR(100) NOT NULL,
    PARAMETER_TYPE VARCHAR(100) NOT NULL,
    PARAMETER_VALUE VARCHAR(2500),
    IDENTIFYING CHAR(1) NOT NULL,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
     references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)                                  
)
```
- JOB_EXECUTION_ID : Job 실행 아이디, 이것은 BATCH_JOB_EXECUTION 테이블의 기본키인 JOB_EXECUTION_ID를 외래키로 사용, 각 실행마다 여러 행(키/값)이 저장됩니다.
- PARAMETER_NAME : 파라미터 이름
- PARAMETER_TYPE : 파라미터 타입
- PARAMETER_VALUE : 파라미터 값
- IDENTIFYING : 파라미터가 JobInstance의 유니크성을 위해 사용된 파라미터라면 true로 세팅됩니다.

### BATCH_STEP_EXECUTION Table
- BATCH_STEP_EXECUTION Table은 StepExecution과 관련된 모든정보를 가집니다.
- 여러면에서 BATCH_JOB_EXECUTION과 유사, 생성된 각 JobExecution에 대한 단계당 항목이 하나이상 존재합니다.
```sql
CREATE TABLE BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY ,
    VERSION BIGINT NOT NULL,
    STEP_NAME VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL, 
    START_TIME TIMESTAMP DEFAULT NULL,
    END_TIME TIMESTAMP DEFAULT NULL,
    STATUS VARCHAR(10),
    COMMIT_COUNT BIGINT,
    READ_COUNT BIGINT,
    FILTER_COUNT BIGINT,
    WRITE_COUNT BIGINT,
    READ_SKIP_COUNT BIGINT,
    WRITE_SKIP_COUNT BIGINT,
    PROCESS_SKIP_COUNT BIGINT,
    ROLLBACK_COUNT BIGINT,
    EXIT_CODE VARCHAR(20),
    EXIT_MESSAGE VARCHAR(2500),
    LAST_UPDATED TIMESTAMP,
    constraint JOB_EXECUTION_STEP_FK foreign key  (JOB_EXECUTION_ID)
    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
)
```
- STEP_EXECUTION_ID : execution에 대해 유니크한 아이디, 해당 컬럼은 StepExecution 객체의 getId를 통해 조회가 가능합니다.
- STEP_NAME : execution이 귀속된 Step 이름
- JOB_EXECUTION_ID : BATCH_JOB_EXECUTION의 기본키를 외래키로 사용, JobExecution에 StepExecution이 속하는것을 의미합니다. JobExecution에 대해 Step이름은 유니크해야합니다.
- START_TIME : execution이 시작된 시간을 나타냅니다.
- END_TIME : execution이 종료된 시간을 나타냅니다. 현재 수행되고 있지 않는데 해당 컬럼에 값이 비어있다면 이전에 에러 발생 또는 마지막 작업에 대해서 실패한 값이 저장되지 않았음을 의미합니다.
- STATUS : execution의 상태를 표현합니다.COMPLETED, STARTED 및 기타 등 BatchStatus 나열값으로 채워집니다.
- COMMIT_COUNT : execution동안 트랜잭션 커밋된 카운트를 나열합니다.
- READ_COUNT : 실행하는 동안 읽어들인 아이템 수
- FILTER_COUNT : 실행하는 동안 필터된 아이템 수
- WRITE_COUNT : 실행되는 동안 쓰기된 아이템 수
- READ_SKIP_COUNT : 실행하는 동안 읽기 작업에서 스킵된 아이템 수
- WRITE_SKIP_COUNT : 실행하는 동안 쓰기 작업에서 스킵된 아이템 수
- PROCESS_SKIP_COUNT : 실행하는 동안 프로세스가 스킵된 아이템 수
- ROLLBACK_COUNT : 실행되는 동안 롤백된 아이템 수, 재시도를 위한 롤백과 복구 프로시저에서 발생한 건
- EXIT_CODE : 실행되는 동안 종료된 문자열, 커맨드라인 Job이라면 숫자로 변환
- EXIT_MESSAGE : Job이 종료되는 경우  어떻게 종료되었는지 나타내고 가능하면 stack trace값이 남게됩니다.
- LAST_UPDATED : execution이 마지막으로 지속된 시간을 나타내는 타임스탬프입니다.

### BATCH_JOB_EXECUTION_CONTEXT Table
- Job의 ExecutionContext에 대한 모든 정보를 저장합니다.
- 매 JobExecution마다 하나의 JobExecutionContext를 가집니다.
- 특정 작업 실행에 필요한 모든 작업 수준 데이터가 포함되어있습니다.
- 일반적으로 실패 후 중단된 부분부터 시작될 수 있도록 실패 후 검색해야하는 상태를 나타냅니다.

```sql
CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID BIGINT PRIMARY KEY ,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
     references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)                                  
)
```
- JOB_EXECUTION_ID : BATCH_JOB_EXECUTION 테이블의 기본키를 외래키로 사용, 주어진 Execution마다 여러개의 row가 저장됩니다.
- SHORT_CONTEXT : SERIALIZED_CONTEXT의 문자로된 버전입니다.
- SERIALIZED_CONTEXT : 직렬화 된 전체 컨텍스트

### BATCH_STEP_EXECUTION_CONTEXT Table
- Step의 ExecutionContext와 관련된 모든 정보를 가집니다.
- StepExecution 마다 정확히 하나의 ExecutionContext가 있고 특정 Step Execution에 대해서 저장될 필요가 있는 모든 데이터를 저장합니다.
- 일반적으로 JobInstance가 중단된 위치에서 시작할 수 있도록 실패 후 검색해야 하는 상태를 나타냅니다.

```sql
CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT(
    STEP_EXECUTION_ID BIGINT PRIMARY KEY ,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT, 
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
    REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)                                    
)
```
- STEP_EXECUTION_ID : BATCH_STEP_EXECUTION 테이블의 기본키를 외래키로 사용, 주어진 Execution마다 여러개의 row가 저장됩니다.
- SHORT_CONTEXT : SERIALIZED_CONTEXT의 문자로된 버전입니다.
- SERIALIZED_CONTEXT : 직렬화 된 전체 컨텍스트

## SpringBatch Sequence
- 스프링 배치는 기본적으로 시퀀스 테이블이 존재합니다.

### BATCH_JOB_SEQ
- 배치 Job에 대한 시퀀스 테이블
- ID
  - bigint
  - 배치 Job의 기본키를 의미
- UNIQUE KEY
  - char(1)
  - 배치 Job 시퀀스를 구별하는 유니크 PK

### BATCH_JOB_EXECUTION_SEQ
- 배치 Job execution 시퀀스 테이블
- ID
  - bigint
  - 배치 Job execution의 기본키를 의미
- UNIQUE KEY
  - char(1)
  - 배치 Job execution 시퀄스를 구별하는 유니크 PK

### BATCH_STEP_EXECUTION_SEQ
- 배치 Step execution 시퀀스 테이블
- ID
    - bigint
    - 배치 Job execution의 기본키를 의미
- UNIQUE KEY
    - char(1)
    - 배치 Job execution 시퀄스를 구별하는 유니크 PK

- 위 시퀀스들을 통해 BATCH_JOB_INSTANCE, BATCH_EXECUTION, BATCH_STEP_EXECUTION의 시퀀스를 배치가 할당하며, 중복될 수 없습니다.


## 스프링 배치 모델
- 스프링 배치는 DI와 AOP를 지원하는 배치 프레임워크입니다.
- 모델
  - Tasklet
    - 로직 자체가 단수한 경우에 사용하는 단순 처리 모델
    - 다양한 데이터 소스나 파일을 한번에 처리해야하는 경우 사용.
  - Chunk
    - 데이터 량이 매우 큰 경우 효과적으로 처리 가능
    - Reader/Processor/Writer 플로우 방식으로 처리

## 스프링 배치 기본 아키텍처

![img.png](스프링%20배치%20아키텍처.png)

- Job
  - 일괄 적용을 위한 일련의 프로세스를 요약하는 단일 실행 단위
- Step
  - Job을 구성하는 처리 단위
  - 하나의 Job에는 여러 Step 구성 가능
  - 하나의 Job에 여러 Step을 재사용, 병렬화, 조건 분기 등을 수행 가능
  - Step은 Tasklet 모델, Chunk 모델의 구현체가 탑재 되어 실행
- JobLauncher
  - Job을 수행하기 위한 인터페이스
  - JobLauncher은 사용자에 의해 직접 수행
  - 자바 커맨드를 통해 CommandLineJobRunner를 실행하여 단순하게 배치 프로세스 수행 가능.
- ItemReader
  - 청크단위 모델에서 사용
  - 소스 데이터를 읽어 들이는 역할 수행
- ItemProcessor
  - 읽어 들인 청크 데이터를 처리
  - 데이터 변환 또는 데이터를 정재하는 등의 역할을 담당
  - 옵션으로 필요없다면 사용하지 않아도 됨.
- ItemWriter
  - 청크 데이터를 읽어들였거나, 처리된 데이터에 대해서 실제 쓰기 작업을 담당
  - 데이터베이스에 저장 및 수정 또는 파일로 처리결과 출력
- Tasklet
  - 단순하고 유연하게 배치 처리를 수행하는 태스크를 수행
- JobRepository
  - Job과 Step의 상태를 관리하는 시스템
  - 스프링 배치에서 사용하는 테이블 스키마를 기반으로 상태정보를 저장하고 관리

## 스프링 배치 흐름

![img.png](스프링%20배치%20흐름.png)

### 처리흐름 관점
1. JobScheduler가 배치를 트리거링 하면 JobLauncher를 실행합니다.
2. JobLauncher는 Job을 실행핣니다. 이때 JobExecution을 수행하고 Execution Context 정보를 이용합니다.
3. Job은 자신에게 설정된 Step을 실행합니다. 이때 StepExecution을 수행하고 Execution Context 정보가 전달되어 수행됩니다.
4. Step은 Tasklet과 Chunk 모델을 가지고 있으며 위 그림에서는 Chunk 모델로 수행되게 됩니다.
5. ItemReader를 통해서 소스 데이터를 읽습니다.
6. ItemProcessor를 통해서 읽은 청크 단위 데이터를 처리합니다. 처리는 데이터를 변환 또는 가공하는 역할을 합니다.
7. ItemWriter은 처리된 청크 데이터를 쓰기 작업합니다. 다양한 Writer를 통해 데이터베이스에 저장하거나 파일로 쓰는 역할을 합니다.

### Job 정보의 흐름 관점
1. JobLauncher는 JobRepository를 통해서 JobInstance 정보를 데이터베이스에 등록합니다.
2. JobLauncher는  Job Execution을 통해 Job을 수행하고 JobRepository를 통해 실행 정보를 데이터베이스에 저장합니다.
3. JobStep은 JobRepository를 통해서 I/O 레코드와 상태정보를 저장합니다.
4. Job이 완료되면 JobRepository를 통해서 데이터베이스에 완료 정보를 저장합니다.

### 스프링배치 저장 정보
- JobInstance
  - Job 이름과 전달 파라미터를 정의합니다.
  - Job이 중단되는 경우 다음 실행할때 중단 이후부터 실행하도록 지원합니다.
  - Job이 재실행을 지원하지 않는 경우 또는 성공적으로 처리된 경우 배치를 재실행한다면 중복 수행되지 않도록 종료합니다.
- JobExecution / ExecutionContext
  - JobExecution
    - Job의 물리적인 실행을 나타냅니다.
    - JobInstance와 달리 동일한 Job이 여러번 수행될 수 있습니다.
    - JobInstance와 JobExecution은 1:N 관계가 됩니다.
  - ExecutionContext
    - 각각의 JobExecution에서 처리 단계와 같은 메타 정보들을 공유하는 영역입니다.
    - 주로 스프링 배치가 프레임워크 상태를 기록하는데 사용하며 애플리케이션에서 ExecutionContext에 엑세스하는 수단도 제공됩니다.
    - ExecutionContext에 저장되는 객체는 java.io.Serialized를 구현하는 클래스이어야합니다.
- StepExecution / ExecutionContext
  - StepExecution
    - Step을 물리적인 실행을 나타냅니다.
    - Job은 여러 Step을 수행하므로 1 : N 관계가 됩니다.
  - ExecutionContext
    - Step 내부에 데이터를 공유해야하는 공유 영역입니다.
    - 데이터의 지역화 관점에서 여러 단계에 공유할 필요 없는 정보는 Job내 ExecutionContext를 이용하는 대신에 Step 단계 내의 ExecutionContext를 사용해야합니다.
    - StepExeuctionContext에 저장되는 데이터는 반드시 java.io.Serialized를 구현하는 클래스이어야합니다.
- JobRepository
  - JobExecution과 StepExecution등과 같이 배치 실행정보, 상태, 결과정보들을 데이터베이스에 저장하는 역할입니다.
  - 스프링 배치를 수행하기 위해서 배치 실행정보, 상태, 결과정보를 저장할 데이터베이스가 필요하고 저장된 정보를 활용하여 배치 잡을 재실행하거나 정지된 상태 후부터 수행할 수 있는 수단을 제공합니다.

## Chunk 모델
- 일정한 단위(청크)로 데이터를 처리하는 방식입니다.
- ChunkOrientedTasklet은 청크 처리를 지원하는 Tasklet의 구현체입니다.
- commit-interval 설정값을 통해서 청크에 포함 될 최대 레코드 수를 조정할 수 있습니다.
- ItemReader, ItemProcessor, ItemWriter은 청크 단위를 처리하기 위한 인터페이스입니다.

![img.png](Chunk모델%20처리과정.png)

- ChunkBaseTasklet은 ItemReader, ItemProcessor, ItemWriter 구현체를 각각 호출합니다.
- ChunkBaseTasklet은 청크 단위에 따라 ItemReader, ItemProcessor, ItemWriter를 반복 실행합니다.
- ItemReader은 청크 단위 만큼 데이터를 읽고 ItemProcessor로 전달하여 ItemProcessor이 데이터를 처리합니다.
- 처리하고 난 청크 단위의 데이터는 ItemWriter로 전달되어 데이터가 저장되거나 파일 처리 작업을 수행합니다.


## ItemReader 제공하는 구현체
- FlatFileItemReader
  - 플랫 파일(구조화 되지 않은 파일)을 읽습니다. e.g) CSV
  - 읽어 들인 데이터를 객체로 매핑하기 위해서 delimeter를 기준으로 매핑 룰을 이용하여 객체로 매핑합니다.
  - 입력에 대해서 Resource Object를 이용하여 커스텀하게 매핑 할 수 있습니다.
- StaxEventItemReader
  - XML 파일 StAX기반으로 읽습니다.
- JdbcPagingItemReader / JdbcCursorItemReader
  - Jdbc를 사용하여 SQL을 실행하여 나온 데이터를 읽습니다.
  - 데이터베이스에서 많은 양의 데이터를 처리해야 하는 경우에는 메모리에 있는 모든 레코드를 읽는 것을 피하고, 한 번의 처리에 필요한 데이터만 읽고 폐기하는 것이 필요하다.
  - JdbcPagingItemReader는 JdbcTemplate을 이용하여 페이징 처리하는 방식으로 구현됩니다.
  - JdbcCursorItemReader는 JDBC 커서를 이용하여 하나의 SELECT SQL을 발행하여 구현됩니다.
- MyBatisPagingItemReader / MyBatisCursorItemReader
  - MyBatis를 이용하여 데이터를 읽습니다.
  - JdbcCursorItemReader, JdbcPagingItemReader와 구현 방식만 다를 뿐 동일합니다.
  - Jpa를 이용해서 데이터를 읽어올 수 있는 방법으로 JpaPagingItemReader, HibernatePagingItemReader, HibernateCursor제공합니다.
- JmsItemReader / AmqpItemReader
  - 메시지를 JMS혹은 AMQP에서 읽어들입니다.

## ItemProcessor 제공하는 구현체
- PassThroughItemProcessor
  - 아무 작업을 수행하지 않아 입력된 데이터의 변경 또는 처리가 필요하지 않는 경우에 사용합니다.
- ValidatingItemProcessor
  - 입력된 데이터를 체크합니다.
  - 입력된 데이터 유효성 검증 규칙을 구현하려면 batch에서 제공하는 org.springframework.batch.item.Validator를 구현해야합니다.
  - 일반적으로 org.springframework.validation.Validator의 어댑터인 SpringValidator와 org.springframework.validation의 규칙을 제공합니다.
- CompositeItemProcessor
  - 동일한 입력 데이터에 대해 여러 ItemProcessor를 순차적으로 실행합니다.
  - ValidatingItemProcessor를 사용하여 입력 확인을 수행한 . 비즈니스 로직을 실행하려는 경우 활성화 됩니다.

## ItemWriter 제공하는 구현체
- FlatFileWriter
  - 처리된 Java 객체를 CSV 파일과 같은 플랫 파일로 작성합니다.
  - 파일 라인에 대한 매핑 규칙은 구분 기호 및 개체에서 사용자 정의로 사용할 수도 있습니다.
- StaxEventItemWriter
  - XML 파일로 자바 객체를 쓸 수 있습니다.
- JdbcBatchItemWriter
  - Jdbc를 사용하여 자바 객체를 데이터베이스에 저장할 수 있습니다.
  - 내부적으로 JdbcTemplate을 사용하게 됩니다.
- MyBatisBatchItemWriter
  - MyBatis를 사용하여 자바 객체를 데이터베이스에 저장할 수 있습니다.
  - MyBatis-Spring은 MyBatis에서 제공하는 라이브러리를 이용합니다.
- JmsItemWriter / AmqpItemWriter
  - JMS 또는 AMQP로 자바 객체의 메시지를 전송합니다.

## Tasklet 모델
- Chunk 모델과 다르게 하나의 레코드만 읽어서 처리해야하는 경우에 Tasklet 모델을 사용합니다.
- 청크 단위로 처리하는게 알맞지 않을 경우에 Tasklet 모델이 유용합니다.
- Tasklet 모델을 사용할때 Spring Batch에서 제공하는 Tasklet 인터페이스를 구현해야합니다.


## Tasklet 구현 클래스
- SystemCommandTasklet
  - 시스템 명령어를 비동기적으로 실행하는 구현체입니다.
  - 명령어를 지정하여 사용할 수 있고 시스템 명령은 호출하는 스레드와 다른 스레드에 의해 실행되므로 실행되는 도중 타임아웃을 설정하고 시스템 명령을 수행하는 스레드를 취소할 수 있습니다.
- MethodInvokingTaskletAdapter
  - POJO 클래스의 특정 메서드를 실행하기 위한 Tasklet 구현체입니다.
  - targetObject 속성에 클래스의 빈을 지정하고, targetMethod 속성에 실행할 메소드 이름을 지정합니다.
  - POJO 클래스는 일괄 처리 종료 상태를 메서드의 반환값으로 반환이 가능하지만 ExitStatus를 반환값으로 설정해야합니다.
  - 다른 타입의 값이 반환될 경우 반환값과 상관없이 정상종료(COMPLETED) 상태로 간주됩니다.