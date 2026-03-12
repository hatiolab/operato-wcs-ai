# 보안 개선 권장사항

> 분석일: 2026-03-05 | 대상: operato-wcs-ai

---

## 1. 취약 라이브러리 교체

### 1.1 ~~FastJSON 1.2.47~~ (완료 ✅ 2026-03-05)

operato-wcs-ai 및 otarepo-core 전체에서 `com.alibaba.fastjson` import가 0건으로 확인되어 `build.gradle`에서 의존성을 안전하게 제거 완료. RCE 취약점(CVE-2022-25845 등) 해소.

### 1.2 ~~iText 4.2.2~~ (완료 ✅ 2026-03-05)

`com.lowagie:itext:4.2.2` POM 의존성을 제거했습니다. `com.github.librepdf:openpdf:1.3.35`가 동일 패키지를 제공하며, 코드베이스에서 직접 import가 없어 안전하게 제거 완료.

### 1.3 Commons Collections 3.2.2 (위험도: High)

**문제**: Java 역직렬화 공격에 악용 가능한 gadget chain이 존재합니다.

**현재 위치**: `build.gradle` 98행

**사용 현황 확인 결과** (2026-03-05):
- `operato-wcs-ai` 소스에서는 직접 사용 없음
- `otarepo-core` DBIST ORM에서 5개 파일이 사용 중 (`LRUMap`, `ListOrderedMap`, `ComparatorUtils`)
- 단순 제거 불가 — otarepo-core 코드 수정 필요

**조치 방법**:
1. otarepo-core의 5개 파일에서 Commons Collections 4.x API로 마이그레이션
2. `build.gradle`에서 `commons-collections:3.2.2` → `org.apache.commons:commons-collections4:4.4`로 교체

---

## 2. 인증/인가 보안

### 2.1 비밀번호 인코더 (위험도: Medium)

**문제**: SHA-256 해시 사용 중. 무차별 대입 공격에 취약합니다.

**권장**: bcrypt, scrypt, 또는 PBKDF2로 교체
```java
// 변경 전
@Bean
public PasswordEncoder passwordEncoder() {
    return new MessageDigestPasswordEncoder("SHA-256");
}

// 변경 후
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 2.2 ~~Deprecated 보안 설정~~ (완료 ✅ 2026-03-05)

`management.security.enabled`, `security.basic.enabled` 설정을 주석 처리하고 Spring Security 6 SecurityFilterChain으로 관리하도록 변경 완료.

---

## 3. 민감 정보 보호

### 3.1 Jasypt 암호화 (완료)

- DB/SMTP 비밀번호에 Jasypt 암호화 적용 완료
- 알고리즘: `PBEWITHHMACSHA512ANDAES_256`
- 마스터 키: 환경변수 `JASYPT_ENCRYPTOR_PASSWORD`로 관리

### 3.2 ~~Shutdown 엔드포인트 노출~~ (완료 ✅ 2026-03-05)

`application.properties`에 `management.endpoint.shutdown.enabled=false` 기본값을 추가하여 프로덕션에서 비활성화. dev 프로파일에서만 오버라이드하여 활성화.

---

## 4. SQL Injection 방어 (양호)

현재 코드베이스의 SQL 접근은 모두 파라미터화되어 있습니다.

```java
// 안전한 패턴 (현재 사용 중)
condition.addFilter("batchId", batchId);
queryManager.executeBySql(
    "UPDATE RACKS SET STATUS = null WHERE DOMAIN_ID = :domainId", params);
```

SQL Injection 위험: **없음**

---

## 5. 구버전 라이브러리 업그레이드

| 라이브러리 | 현재 | 위험도 | 권장 버전 |
|-----------|------|--------|----------|
| Apache Velocity | 1.7 | Medium | 2.3+ |
| Commons DBCP | 1.4 | Medium | HikariCP |
| ~~Apache Batik~~ | ~~1.14~~ | ~~Low~~ | ✅ 1.17로 업그레이드 완료 |
| ~~Hutool~~ | ~~5.7.20~~ | ~~Low~~ | ✅ 제거 완료 (미사용 확인) |
| ~~Groovy JSR223~~ | ~~5.0.0-alpha-4~~ | ~~Low~~ | ✅ 4.0.24로 교체 완료 |

---

## 6. Docker 보안

### 양호 항목
- 비root 사용자(spring) 생성 및 실행
- 멀티스테이지 빌드로 빌드 도구 미포함
- 헬스 체크 설정

### 개선 필요
- 의존성 취약점 스캐닝 미적용 (Trivy, Snyk 등 도입 권장)
- SBOM(Software Bill of Materials) 미생성

---

## 7. 우선순위 요약

| 우선순위 | 항목 | 난이도 |
|---------|------|--------|
| ~~P0~~ | ~~FastJSON 제거 (미사용 확인)~~ | ✅ 완료 |
| ~~P0~~ | ~~iText 제거 (OpenPDF로 전환)~~ | ✅ 완료 |
| P1 | Commons Collections 4.x 업그레이드 | 중 (otarepo-core 수정 필요) |
| P1 | 비밀번호 인코더 bcrypt 전환 | 하 |
| ~~P1~~ | ~~Deprecated 보안 설정 제거~~ | ✅ 완료 |
| ~~P2~~ | ~~Shutdown 엔드포인트 접근 제한~~ | ✅ 완료 |
| P2 | 구버전 라이브러리 업그레이드 | 중 |
| P3 | Docker 취약점 스캐닝 도입 | 중 |
