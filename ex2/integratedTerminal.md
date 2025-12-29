# Debug Console 출력 설정 가이드

## 개요

이 문서는 Spring Boot 프로젝트에서 테스트 실행 및 디버깅 시 출력을 VS Code/Cursor의 Debug Console로 전송하도록 설정하는 방법을 설명합니다.

## 설정 파일 위치

### 1. VS Code/Cursor 설정 파일
- **경로**: `.vscode/settings.json` (워크스페이스 루트)
- **목적**: Java 확장 프로그램의 테스트 및 디버깅 출력을 Debug Console로 리다이렉트

### 2. Gradle 설정 파일
- **경로**: `ex2/gradle.properties`
- **목적**: Gradle 빌드 시 콘솔 출력 형식 설정

### 3. Build 설정 파일
- **경로**: `ex2/build.gradle`
- **목적**: 테스트 실행 시 표준 출력 스트림 표시 설정

---

## 설정 내용

### 1. `.vscode/settings.json`

```json
{
    "java.configuration.updateBuildConfiguration": "interactive",
    "java.compile.nullAnalysis.mode": "automatic",
    "java.test.config": {
        "console": "internalConsole"
    },
    "java.debug.settings.console": "internalConsole"
}
```

#### 주요 설정 설명

- **`java.test.config.console`**: `"internalConsole"`
  - JUnit 테스트 실행 시 출력을 Debug Console로 전송
  - `"internalConsole"`은 Debug Console을 의미합니다
  - 다른 옵션: `"integratedTerminal"` (Integrated Terminal), `"externalTerminal"` (외부 터미널)

- **`java.debug.settings.console`**: `"internalConsole"`
  - Java 디버깅 시 출력을 Debug Console로 전송
  - 로그 및 System.out.println() 출력이 Debug Console에 표시됨
  - 디버깅 모드에서 실행할 때 출력이 Debug Console 탭에 나타납니다

### 2. `ex2/gradle.properties`

```properties
# 인코딩 설정
org.gradle.jvmargs=-Dfile.encoding=UTF-8 -Duser.country=KR -Duser.language=ko
org.gradle.console=plain
```

#### 주요 설정 설명

- **`org.gradle.console=plain`**
  - Gradle 빌드 출력을 일반 텍스트 형식으로 표시
  - 다른 옵션: `auto`, `rich`, `verbose`

### 3. `ex2/build.gradle` (테스트 설정)

```groovy
tasks.named('test') {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
        showStandardStreams = true  // 표준 출력 스트림 표시
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
}
```

#### 주요 설정 설명

- **`showStandardStreams = true`**
  - 테스트 실행 시 System.out.println() 및 System.err.println() 출력을 표시
  - Debug Console에서 테스트 로그와 함께 출력됨

---

## 설정 적용 방법

### 1. 설정 파일 수정 후

1. **VS Code/Cursor 재시작**
   - 설정 변경 사항을 완전히 적용하기 위해 IDE를 재시작하는 것이 좋습니다.

2. **Java Language Server 재시작** (선택사항)
   - Command Palette (`Ctrl+Shift+P`) → "Java: Clean Java Language Server Workspace"
   - 또는 "Developer: Reload Window"

### 2. 테스트 실행 확인

#### 방법 1: 테스트 파일에서 직접 실행
- 테스트 파일 상단의 "Run Test" 버튼 클릭
- 또는 테스트 메서드 위의 "Run Test" 링크 클릭

#### 방법 2: Command Palette 사용
- `Ctrl+Shift+P` → "Java: Run Tests"
- Debug Console에서 테스트가 실행되고 출력이 표시됩니다.

#### 방법 3: Gradle 명령어 사용
```bash
# Windows PowerShell
.\gradlew test

# 또는 특정 테스트만 실행
.\gradlew test --tests "org.zerock.ex2.repository.MemoRepositoryTests"
```

---

## 출력 확인 사항

### Debug Console에서 확인할 수 있는 출력

**중요**: Debug Console은 VS Code/Cursor 하단의 "DEBUG CONSOLE" 탭에서 확인할 수 있습니다. 디버깅 모드로 실행하거나 테스트를 실행할 때 자동으로 열립니다.

1. **테스트 실행 결과**
   - 테스트 통과/실패/스킵 상태
   - 실행 시간
   - 테스트 메서드별 상세 정보

2. **표준 출력 (System.out.println)**
   - 테스트 코드 내의 System.out.println() 출력
   - 로그 메시지

3. **에러 및 스택 트레이스**
   - 테스트 실패 시 전체 스택 트레이스
   - 예외 메시지 및 원인

4. **SQL 쿼리 출력** (설정된 경우)
   - JPA/Hibernate SQL 쿼리
   - 파라미터 바인딩 정보

---

## 문제 해결

### 문제 1: Debug Console에 출력이 표시되지 않음

**해결 방법:**
1. **Debug Console 탭 확인**
   - VS Code/Cursor 하단의 "DEBUG CONSOLE" 탭이 열려있는지 확인
   - View → Debug Console 메뉴로 열 수 있습니다

2. **디버깅 모드로 실행**
   - F5 키를 눌러 디버깅 모드로 실행
   - 또는 Run and Debug 패널에서 디버깅 시작

3. **VS Code/Cursor 재시작**
   - 설정 변경 사항을 완전히 적용하기 위해 IDE를 재시작

4. **설정 파일 확인**
   - `.vscode/settings.json` 파일이 워크스페이스 루트에 있는지 확인
   - `"internalConsole"` 값이 올바르게 설정되었는지 확인

5. **Java 확장 프로그램 확인**
   - Java 확장 프로그램이 최신 버전인지 확인

### 문제 2: 한글이 깨져서 표시됨

**해결 방법:**
1. `gradle.properties`의 인코딩 설정 확인:
   ```properties
   org.gradle.jvmargs=-Dfile.encoding=UTF-8 -Duser.country=KR -Duser.language=ko
   ```

2. PowerShell 인코딩 설정:
   ```powershell
   [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
   chcp 65001
   ```

### 문제 3: 테스트 출력이 보이지 않음

**해결 방법:**
1. `build.gradle`의 `showStandardStreams = true` 설정 확인
2. 테스트 실행 시 `--info` 또는 `--debug` 플래그 사용:
   ```bash
   .\gradlew test --info
   ```

---

## 추가 설정 (선택사항)

### 로깅 레벨 설정

테스트 환경에서 더 자세한 로그를 보려면 `src/test/resources/application.properties`에 다음을 추가:

```properties
# 로깅 레벨 설정
logging.level.org.zerock.ex2=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### 터미널 색상 출력

Gradle에서 색상 출력을 활성화하려면 `gradle.properties`에 추가:

```properties
org.gradle.console=rich
```

---

## 참고 자료

- [VS Code Java Extension Documentation](https://code.visualstudio.com/docs/java/java-testing)
- [Gradle Test Logging](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html)
- [Spring Boot Testing Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

## 설정 요약

| 설정 파일 | 주요 설정 | 목적 |
|----------|----------|------|
| `.vscode/settings.json` | `java.test.config.console: "internalConsole"` | 테스트 출력을 Debug Console로 전송 |
| `.vscode/settings.json` | `java.debug.settings.console: "internalConsole"` | 디버깅 출력을 Debug Console로 전송 |
| `ex2/gradle.properties` | `org.gradle.console=plain` | Gradle 출력 형식 설정 |
| `ex2/build.gradle` | `showStandardStreams = true` | 테스트 표준 출력 스트림 표시 |

## Console 옵션 비교

| 옵션 | 설명 | 사용 시기 |
|------|------|----------|
| `"internalConsole"` | Debug Console에 출력 | 디버깅 시 출력을 IDE 내부에서 확인하고 싶을 때 |
| `"integratedTerminal"` | Integrated Terminal에 출력 | 터미널에서 직접 확인하고 싶을 때 |
| `"externalTerminal"` | 외부 터미널 창에 출력 | 별도의 터미널 창에서 확인하고 싶을 때 |

---

**마지막 업데이트**: 2024년
**프로젝트**: ex2 (Spring Boot 3.4.13)

