# 보안 개선 권장사항

## 즉시 수정이 필요한 심각한 보안 취약점

### 1. 취약한 라이브러리 업그레이드

#### 현재 문제
```gradle
// ❌ 심각한 보안 취약점 존재
implementation 'commons-collections:commons-collections:3.2.2'  // CVE-2015-7501 (RCE)
implementation 'com.alibaba:fastjson:1.2.47'                    // Deserialization RCE
implementation 'commons-dbcp:commons-dbcp:1.4'                  // 오래됨 (2011년)
implementation 'org.apache.velocity:velocity:1.7'               // SSTI 취약점
```

#### 개선 방법
```gradle
// ✅ 수정 후
// commons-collections 3.x 완전 제거 또는:
implementation 'org.apache.commons:commons-collections4:4.4'

// fastjson 업그레이드 (또는 제거하고 Jackson 사용)
implementation 'com.alibaba:fastjson:1.2.83'  // 최소 버전

// commons-dbcp 제거 (Spring Boot 기본 HikariCP 사용)
// - HikariCP는 spring-boot-starter-jdbc에 포함됨

// velocity 업그레이드
implementation 'org.apache.velocity:velocity-engine-core:2.3'
```

### 2. 민감 정보 암호화

#### 현재 문제
`src/main/resources/application-dev.properties` 파일에 민감 정보가 평문으로 저장됨:

```properties
# ❌ 심각: 평문 저장
spring.datasource.username=anythings
spring.datasource.password=anythings
mail.smtp.password=1q2w3e4r~!
mq.broker.user.pw=admin

# ❌ 실제 IP 노출
spring.datasource.url=jdbc:oracle:thin:@60.196.69.234:20000:orcl
mq.broker.address=60.196.69.234
```

#### 개선 방법 1: Jasypt 활용 (이미 의존성 있음)

프로젝트에 이미 `jasypt-spring-boot-starter:3.0.4`가 포함되어 있으므로 즉시 사용 가능:

```bash
# 1. 암호화 키 생성
export JASYPT_ENCRYPTOR_PASSWORD="your-secret-key-here"

# 2. 값 암호화 (Jasypt CLI 사용)
java -cp jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
     input="anythings" \
     password="${JASYPT_ENCRYPTOR_PASSWORD}" \
     algorithm=PBEWithMD5AndDES
```

```properties
# ✅ 암호화된 설정
spring.datasource.username=ENC(encrypted_username_here)
spring.datasource.password=ENC(encrypted_password_here)
mail.smtp.password=ENC(encrypted_email_password_here)
```

`application.yml` 설정:
```yaml
jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
    algorithm: PBEWithMD5AndDES
```

#### 개선 방법 2: 환경 변수 사용

```properties
# ✅ 환경 변수 참조
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
mail.smtp.password=${EMAIL_PASSWORD}
mq.broker.user.pw=${MQ_PASSWORD}
```

서버 실행 시:
```bash
export DB_USERNAME=anythings
export DB_PASSWORD=secret_password
export EMAIL_PASSWORD=secret_email_password
export MQ_PASSWORD=secret_mq_password

java -jar operato-wcs-ai.jar
```

#### 개선 방법 3: Spring Cloud Config (운영 환경 권장)

```yaml
# bootstrap.yml
spring:
  cloud:
    config:
      uri: https://config-server.example.com
      name: operato-wcs
      profile: ${spring.profiles.active}
```

### 3. Spring Security 강화

#### 현재 문제
```java
// SecurityConfigration.java
// ❌ 모든 요청 허용 상태
http.authorizeHttpRequests()
    .anyRequest().permitAll();  // 누구나 접근 가능

// ❌ JWT 인증 필터 주석 처리
// .addFilterBefore(jwtAuthenticationFilter(), ...)
```

#### 개선 방법

```java
@Configuration
@EnableWebSecurity
public class SecurityConfigration {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic().disable()
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                // ✅ Public 엔드포인트
                .requestMatchers("/rest/login", "/rest/refresh", "/actuator/health").permitAll()

                // ✅ 관리자 전용
                .requestMatchers("/rest/admin/**").hasRole("ADMIN")

                // ✅ 인증 필수
                .requestMatchers("/rest/**").authenticated()

                // ✅ 기타 모든 요청 거부
                .anyRequest().denyAll()
            .and()
            // ✅ JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            // ✅ 보안 헤더 추가
            .headers()
                .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';")
                .and()
                .xssProtection()
                .and()
                .frameOptions().deny()
                .and()
                .contentTypeOptions();

        return http.build();
    }
}
```

### 4. CORS 설정

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/rest/**")
                .allowedOrigins("https://your-frontend-domain.com")  // ✅ 명시적 도메인 지정
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### 5. Rate Limiting 추가

```gradle
// build.gradle에 추가
implementation 'com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0'
implementation 'com.github.vladimir-bukhtoyarov:bucket4j-jcache:7.6.0'
```

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = request.getRemoteAddr();
        Bucket bucket = cache.computeIfAbsent(key, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);  // Too Many Requests
            response.getWriter().write("Rate limit exceeded");
        }
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
```

---

## 체크리스트

### 즉시 수정 (1주 이내)
- [ ] commons-collections 3.2.2 제거/업그레이드
- [ ] fastjson 1.2.47 업그레이드 (→ 1.2.83+) 또는 제거
- [ ] commons-dbcp 제거 (HikariCP 사용)
- [ ] velocity 1.7 업그레이드 (→ 2.3)
- [ ] 민감 정보 암호화 (Jasypt 또는 환경 변수)
- [ ] application-*.properties에서 실제 IP 주소 제거
- [ ] Spring Security 권한 검증 활성화
- [ ] JWT 인증 필터 활성화

### 1개월 이내
- [ ] CORS 정책 명시적 설정
- [ ] Rate Limiting 적용
- [ ] 보안 헤더 추가 (CSP, X-Frame-Options 등)
- [ ] 보안 감사(Audit) 로깅 추가
- [ ] HTTPS 강제 (운영 환경)
- [ ] Spring Cloud Config 도입 고려

---

## 참고 자료

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Jasypt Spring Boot](https://github.com/ulisesbocchio/jasypt-spring-boot)
- [CVE-2015-7501 (commons-collections)](https://nvd.nist.gov/vuln/detail/CVE-2015-7501)
- [Fastjson 취약점 목록](https://github.com/alibaba/fastjson/wiki/security_update)
