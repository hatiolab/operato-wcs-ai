---
name: deploy-backend
description: Spring Boot 백엔드를 빌드하고 배포 가능한 JAR 파일을 생성합니다. Java 버전 자동 설정 및 빌드 검증을 수행합니다.
disable-model-invocation: false
argument-hint: "[옵션: --with-tests | --no-clean]"
---

# 백엔드 빌드 및 배포

Spring Boot 애플리케이션을 빌드하여 배포 가능한 JAR 파일을 생성합니다.

## 실행 절차

### 1. 환경 확인 및 설정

1. **Java 버전 확인**
   - 현재 Gradle이 사용 중인 Java 버전 확인: `./gradlew --version`
   - Spring Boot 3.2.4는 **Java 17 이상** 필요

2. **Java 버전 자동 전환**
   - Java 8 또는 11이 감지되면 Java 18로 자동 전환
   - 명령어: `export JAVA_HOME=$(/usr/libexec/java_home -v 18)`
   - Java 18이 없으면 사용 가능한 최신 버전 사용

### 2. 빌드 실행

**기본 빌드 명령어** (테스트 스킵):
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 18) && ./gradlew clean build -x test
```

**옵션별 명령어**:
- `--with-tests`: 테스트 포함 빌드
  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 18) && ./gradlew clean build
  ```

- `--no-clean`: clean 스킵 (증분 빌드)
  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 18) && ./gradlew build -x test
  ```

### 3. 빌드 검증

빌드 완료 후 다음을 확인:

1. **빌드 성공 여부**
   - `BUILD SUCCESSFUL` 메시지 확인
   - 에러 또는 경고 메시지 수집

2. **생성된 JAR 파일**
   ```bash
   ls -lh build/libs/
   ```
   - `operato-wcs-ai.jar` — 실행 가능한 JAR (모든 의존성 포함)
   - `operato-wcs-ai-plain.jar` — 의존성 제외 JAR

3. **파일 크기 확인**
   - 정상적인 빌드: `operato-wcs-ai.jar` 약 150-160MB
   - 비정상적으로 작으면 의존성 누락 가능성 경고

### 4. 결과 보고

사용자에게 다음 정보를 제공:

```
✅ **빌드 성공!**

**생성된 파일:**
- build/libs/operato-wcs-ai.jar — 157MB (실행 가능한 JAR)
- build/libs/operato-wcs-ai-plain.jar — 18MB (의존성 제외)

**빌드 정보:**
- Gradle 버전
- Java 버전
- Spring Boot 버전
- 빌드 시간
- 테스트 실행 여부

**경고/에러:**
- (있는 경우 목록 표시)

**실행 방법:**
java -jar build/libs/operato-wcs-ai.jar
```

## 인자 처리

`$ARGUMENTS`로 전달된 옵션을 파싱:

- `--with-tests` 또는 `-t`: 테스트 포함 빌드
- `--no-clean` 또는 `-nc`: clean 단계 스킵
- 인자가 없으면 기본 동작: `clean build -x test`

## 에러 처리

### Java 버전 불일치
- **증상**: "compatible with Java 8" vs "compatible with Java 17" 에러
- **해결**: Java 18로 JAVA_HOME 자동 전환

### 의존성 해결 실패
- **증상**: "Could not resolve" 에러
- **해결**: 네트워크 확인, Gradle 캐시 정리 제안
  ```bash
  ./gradlew clean --refresh-dependencies
  ```

### 컴파일 에러
- **증상**: 소스 코드 컴파일 실패
- **해결**: 에러 메시지 전체 출력 및 수정 방법 제안

### 테스트 실패 (--with-tests 사용 시)
- **증상**: 일부 테스트 실패
- **해결**: 실패한 테스트 목록 출력, `-x test` 옵션 사용 제안

## 주의사항

1. **Java 버전 필수 요구사항**
   - Spring Boot 3.2.4는 Java 17+ 필수
   - 빌드 전 반드시 Java 버전 확인 및 전환

2. **빌드 시간**
   - 전체 빌드: 약 8-15초 (테스트 스킵)
   - 테스트 포함: 시간이 더 소요될 수 있음
   - `--no-clean` 사용 시 증분 빌드로 시간 단축

3. **JAR 파일 크기**
   - 정상: 150-160MB (모든 의존성 포함)
   - 작으면 의존성 문제 가능성

4. **Git 상태**
   - 빌드 산출물은 `.gitignore`에 포함되어 커밋되지 않음
   - `build/`, `.gradle/`, `bin/` 디렉토리는 Git 무시됨

## 사용 예시

```bash
# 기본 빌드 (테스트 스킵, clean 포함)
/deploy-backend

# 테스트 포함 빌드
/deploy-backend --with-tests

# 증분 빌드 (clean 스킵)
/deploy-backend --no-clean

# 테스트 포함 + 증분 빌드
/deploy-backend --with-tests --no-clean
```

## 배포 후 실행

생성된 JAR 파일 실행 방법:

```bash
# 기본 실행
java -jar build/libs/operato-wcs-ai.jar

# 프로파일 지정 (운영 환경)
java -jar -Dspring.profiles.active=prod build/libs/operato-wcs-ai.jar

# 포트 변경
java -jar -Dserver.port=8090 build/libs/operato-wcs-ai.jar
```
