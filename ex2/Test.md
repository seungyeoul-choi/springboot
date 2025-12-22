# Spring Boot JPA 테스트 가이드

## 목차

1. [데이터베이스 설정](#데이터베이스-설정)
2. [테스트 코드 작성](#테스트-코드-작성)
3. [SQL 쿼리 출력 설정](#sql-쿼리-출력-설정)
4. [Gradle 테스트 실행 시 SQL 쿼리 확인](#gradle-테스트-실행-시-sql-쿼리-확인)
5. [주요 문제 해결](#주요-문제-해결)
6. [테스트 메서드별 설명](#테스트-메서드별-설명)

---

## 데이터베이스 설정

### 테스트용 application.properties

테스트 실행 시 SQL 쿼리를 확인하려면 `src/test/resources/application.properties` 파일을 생성합니다.

```properties
# 테스트 환경 데이터베이스 연결 설정
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/bootex
spring.datasource.username=bootuser
spring.datasource.password=bootuser

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect

# 테스트 환경에서 SQL 쿼리 출력 설정
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 로깅 레벨 설정 (SQL 쿼리를 더 명확하게 보기 위해)
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**중요**: 테스트 환경에서는 `src/test/resources/application.properties` 파일이 메인 설정(`src/main/resources/application.properties`)을 오버라이드합니다. 따라서 테스트용 설정 파일에 **반드시 데이터베이스 연결 정보를 포함**해야 합니다.

### 설정 설명

- **`spring.jpa.properties.hibernate.dialect`**: MariaDB를 사용할 때 반드시 설정해야 합니다. 이 설정이 없으면 테이블 생성 시 SQL 오류가 발생할 수 있습니다.
- **`spring.jpa.hibernate.ddl-auto=update`**: 애플리케이션 시작 시 엔티티를 기반으로 테이블을 자동으로 생성/수정합니다.
- **`spring.jpa.show-sql=true`**: SQL 쿼리를 콘솔에 출력합니다.
- **`spring.jpa.properties.hibernate.format_sql=true`**: SQL 쿼리를 읽기 쉽게 포맷팅합니다.
- **`logging.level.org.hibernate.SQL=DEBUG`**: Hibernate SQL 로깅 레벨을 DEBUG로 설정합니다.
- **`logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE`**: 바인딩된 파라미터 값도 함께 출력합니다.

---

## 테스트 코드 작성

### MemoRepositoryTests.java

```java
package org.zerock.ex2.repository;

import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.zerock.ex2.entity.Memo;

import jakarta.transaction.Transactional;

@SpringBootTest
public class MemoRepositoryTests {

    @Autowired
    MemoRepository memoRepository;

    @Test
    public void testInsertDummies() {
        IntStream.rangeClosed(1, 100).forEach(i -> {
            Memo memo = Memo.builder().memoText("Sample..." + i).build();
            memoRepository.save(memo);
        });
    }

    @Test
    public void testSelect() {
        Long mno = 100L;
        Optional<Memo> result = memoRepository.findById(mno);

        System.out.println("================================================");

        if (result.isPresent()) {
            Memo memo = result.get();
            System.out.println(memo);
        }
    }

    @Transactional
    @Test
    public void testSelect2() {
        Long mno = 100L;
        Memo memo = memoRepository.getReferenceById(mno);
        System.out.println("================================================");
        System.out.println(memo);
    }

    @Test
    public void testUpdate() {
        Memo memo = Memo.builder().mno(100L).memoText("Update Text").build();
        System.out.println(memoRepository.save(memo));
    }

    @Test
    public void testDelete() {
        Long mno = 100L;
        memoRepository.deleteById(mno);
    }

    @Test
    public void testPageDefault() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Memo> result = memoRepository.findAll(pageable);
        System.out.println(result);
    }
}
```

---

## SQL 쿼리 출력 설정

### 출력 예시

테스트 실행 시 다음과 같은 형식으로 SQL 쿼리가 출력됩니다:

```
Hibernate:
    delete
    from
        tbl_memo
    where
        mno=?
```

---

## Gradle 테스트 실행 시 SQL 쿼리 확인

### build.gradle 설정

Gradle 테스트 실행 시 터미널에서 SQL 쿼리를 확인하려면 `build.gradle` 파일의 `test` 태스크에 로깅 설정을 추가해야 합니다.

```gradle
tasks.named('test') {
	useJUnitPlatform()
	testLogging {
		events "passed", "skipped", "failed"
		exceptionFormat "full"
		showStandardStreams = true
		showCauses = true
		showExceptions = true
		showStackTraces = true
	}
}
```

### 설정 설명

- **`showStandardStreams = true`**: System.out/System.err 출력을 표시합니다. 이 설정이 없으면 SQL 쿼리가 출력되지 않을 수 있습니다.
- **`showExceptions = true`**: 예외 정보를 표시합니다.
- **`showStackTraces = true`**: 스택 트레이스를 표시합니다.

### 테스트 실행 방법

#### 방법 1: 일반 명령어 실행 (권장)

`build.gradle`에 테스트 로깅 설정을 추가했으므로, 다음 명령어로 실행하면 SQL 쿼리가 터미널에 출력됩니다:

```bash
.\gradlew.bat test --tests org.zerock.ex2.repository.MemoRepositoryTests.testPageDefault
```

#### 방법 2: 더 자세한 로그를 원하는 경우

더 자세한 로그를 보려면 `--info` 플래그를 추가하세요:

```bash
.\gradlew.bat test --tests org.zerock.ex2.repository.MemoRepositoryTests.testPageDefault --info
```

#### 다른 테스트 실행 예시

```bash
# 특정 테스트 실행
.\gradlew.bat test --tests org.zerock.ex2.repository.MemoRepositoryTests.testDelete

# 모든 테스트 실행
.\gradlew.bat test
```

---

## 주요 문제 해결

### 1. getOne() 메서드 제거 문제

**문제**: Spring Boot 3.x에서는 `getOne()` 메서드가 제거되었습니다.

**해결**: `getReferenceById()` 메서드를 사용합니다.

```java
// ❌ 오류 발생
Memo memo = memoRepository.getOne(mno);

// ✅ 올바른 방법
Memo memo = memoRepository.getReferenceById(mno);
```

**주의사항**:

- `getReferenceById()`는 Lazy 프록시를 반환하므로 `@Transactional` 어노테이션이 필요합니다.
- 실제 엔티티에 접근할 때까지 데이터베이스 쿼리가 실행되지 않습니다.

### 2. 테스트 메서드에 @Test 어노테이션 누락

**문제**: `@Test` 어노테이션이 없으면 JUnit이 테스트로 인식하지 않습니다.

**해결**: 모든 테스트 메서드에 `@Test` 어노테이션을 추가합니다.

```java
// ❌ 테스트로 인식되지 않음
public void testUpdate() {
    // ...
}

// ✅ 테스트로 인식됨
@Test
public void testUpdate() {
    // ...
}
```

### 3. 트랜잭션 롤백 문제

**문제**: `@SpringBootTest`를 사용하면 기본적으로 각 테스트 메서드가 끝날 때 트랜잭션이 자동으로 롤백됩니다. 따라서 `deleteById()`를 호출해도 실제 데이터베이스에는 반영되지 않습니다.

**해결**: `@Commit` 어노테이션을 추가하여 테스트 종료 시 트랜잭션을 커밋합니다.

```java
// ❌ 롤백되어 실제 DB에 반영되지 않음
@Test
public void testDelete() {
    Long mno = 100L;
    memoRepository.deleteById(mno);
}

// ✅ 실제 DB에 반영됨
@Commit
@Test
public void testDelete() {
    Long mno = 100L;
    memoRepository.deleteById(mno);
}
```

**참고사항**:

- 테스트 간 데이터 격리가 필요한 경우 `@Commit`을 사용하지 않습니다.
- 실제 데이터베이스에 변경사항을 반영하고 싶을 때만 `@Commit`을 사용합니다.

### 4. DataSourceBeanCreationException 오류

**문제**: 테스트 실행 시 다음과 같은 오류가 발생합니다.

```
java.lang.IllegalStateException: Failed to load ApplicationContext
Caused by: org.springframework.boot.autoconfigure.jdbc.DataSourceProperties$DataSourceBeanCreationException
```

**원인**:

- 테스트 환경의 `src/test/resources/application.properties` 파일에 데이터베이스 연결 설정이 없을 때 발생합니다.
- Spring Boot는 테스트 실행 시 `src/test/resources/application.properties`를 우선적으로 사용하며, 이 파일이 메인 설정을 완전히 오버라이드합니다.
- 따라서 테스트용 설정 파일에 데이터베이스 연결 정보가 없으면 `DataSource` 빈을 생성할 수 없어 오류가 발생합니다.

**해결**: `src/test/resources/application.properties` 파일에 데이터베이스 연결 정보를 추가합니다.

**주의사항**:

- 테스트용 설정 파일은 메인 설정을 완전히 오버라이드하므로, 필요한 모든 설정을 포함해야 합니다.
- 데이터베이스 연결 정보뿐만 아니라 JPA 관련 설정도 함께 포함하는 것이 좋습니다.

---

## 테스트 메서드별 설명

### testInsertDummies()

- 1부터 100까지의 더미 데이터를 생성합니다.
- `IntStream`을 사용하여 반복적으로 `save()` 메서드를 호출합니다.

### testSelect()

- `findById()`를 사용하여 특정 ID의 엔티티를 조회합니다.
- `Optional`을 반환하므로 `isPresent()`로 존재 여부를 확인합니다.

### testSelect2()

- `getReferenceById()`를 사용하여 Lazy 프록시를 반환받습니다.
- `@Transactional`이 필요하며, 실제 엔티티에 접근할 때 쿼리가 실행됩니다.

### testUpdate()

- 기존 엔티티를 수정합니다.
- `save()` 메서드는 ID가 존재하면 UPDATE, 없으면 INSERT를 수행합니다.

### testDelete()

- `deleteById()`를 사용하여 특정 ID의 엔티티를 삭제합니다.
- 실제 DB에 반영하려면 `@Commit` 어노테이션을 추가해야 합니다.

### testPageDefault()

- 페이징 처리를 테스트합니다.
- `PageRequest.of(0, 10)`으로 첫 번째 페이지의 10개 항목을 조회합니다.

---

## 주의사항

1. **트랜잭션 관리**: `@SpringBootTest`는 기본적으로 각 테스트마다 트랜잭션을 롤백합니다.
2. **데이터 격리**: 테스트 간 데이터 격리를 위해 기본 롤백 동작을 유지하는 것이 좋습니다.
3. **실제 DB 반영**: 실제 데이터베이스에 변경사항을 반영해야 하는 경우에만 `@Commit`을 사용합니다.
4. **Lazy Loading**: `getReferenceById()`를 사용할 때는 반드시 `@Transactional`을 사용해야 합니다.
5. **SQL 쿼리 확인**: Gradle 테스트 실행 시 SQL 쿼리를 확인하려면 `build.gradle`에 `showStandardStreams = true` 설정이 필요합니다.
