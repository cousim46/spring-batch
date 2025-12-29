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