# **Spring Boot 프로젝트 가이드**

## **1. 스프링 부트(Spring Boot)란?**

스프링 부트(Spring Boot)는 스프링 프레임워크를 기반으로 **설정을 최소화하고 빠르게 애플리케이션을 개발**할 수 있도록 도와주는 프레임워크입니다.

### **1.1. 스프링 프레임워크 vs 스프링 부트**

| 특징      | 스프링 프레임워크                     | 스프링 부트                             |
| --------- | ------------------------------------- | --------------------------------------- |
| 설정 방식 | XML, Java Config 등 복잡한 설정 필요  | 기본 설정 제공 (자동 설정)              |
| 웹 서버   | 별도 설정 필요                        | 내장 서버 제공 (Tomcat, Jetty 등)       |
| 실행 방식 | WAR 패키징 필요                       | JAR 실행 가능 (독립 실행형)             |
| 주요 기능 | DI, AOP, 트랜잭션 등 다양한 기능 제공 | 기본 설정 + 스타터 패키지로 편리한 개발 |

---

## **2. 프로젝트 생성 방법**

### **2.1. Spring Initializr 사용**

[Spring Initializr](https://start.spring.io/)에서 프로젝트를 생성할 수 있습니다.

- **Project**: Gradle - Groovy
- **Language**: Java
- **Spring Boot Version**: `3.4.1`
- **Dependencies**:
  - Spring Web
  - Spring Boot DevTools
  - Lombok
  - Spring Data JPA
  - H2 Database

설정을 완료한 후 **프로젝트를 다운로드**하고 개발 환경에서 엽니다.

### **2.2. Gradle을 이용한 CLI 생성**

```bash
spring init --name=my-spring-boot-app --dependencies=web,jpa,h2,lombok --build=gradle my-spring-boot-app
```

---

## **3. 프로젝트 구조**

```
my-spring-boot-app
│── src
│   ├── main
│   │   ├── java/com/example/demo
│   │   │   ├── DemoApplication.java  // 메인 클래스
│   │   │   ├── controller/           // API 컨트롤러
│   │   │   ├── service/               // 비즈니스 로직
│   │   │   ├── repository/            // 데이터베이스 처리
│   │   │   ├── entity/                // JPA 엔티티
│   │   │   ├── config/                // 설정 관련 클래스
│   │   ├── resources/
│   │   │   ├── application.yml        // 환경 설정 파일
│   │   │   ├── static/                // 정적 리소스
│   │   │   ├── templates/             // Thymeleaf 템플릿 파일
│── build.gradle                        // Gradle 설정 파일
│── settings.gradle                      // 프로젝트 설정 파일
```

---

## **4. 프로젝트 실행 방법**

### **4.1. 실행 명령어**

```bash
./gradlew bootRun
```

또는 `DemoApplication.java` 실행 버튼 클릭.

### **4.2. 서버 확인**

브라우저에서 아래 주소로 접속하면 정상적으로 실행됨을 확인할 수 있습니다.

```
http://localhost:8080
```

---

## **5. API 개발**

### **5.1. REST API 기본 개념**

- `@RestController` → JSON 형식의 응답을 반환하는 컨트롤러
- `@RequestMapping` → API 엔드포인트 설정
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` → HTTP 메서드 처리

### **5.2. API 엔드포인트**

| 메서드   | URL           | 설명                      |
| -------- | ------------- | ------------------------- |
| `GET`    | `/api/hello`  | 간단한 "Hello World" 반환 |
| `GET`    | `/users`      | 모든 사용자 조회          |
| `POST`   | `/users`      | 사용자 추가               |
| `GET`    | `/users/{id}` | 특정 사용자 조회          |
| `DELETE` | `/users/{id}` | 사용자 삭제               |

API는 `Postman`이나 `cURL`을 이용해 테스트할 수 있습니다.

#### **테스트 예시**

```bash
curl -X GET "http://localhost:8080/api/hello"
```

---

## **6. JPA와 데이터베이스 설정**

### **6.1. JPA 설정 (H2 데이터베이스)**

`application.yml` 파일에서 데이터베이스를 설정할 수 있습니다.

```yaml
spring:
  application:
    name: demo
  datasource:
    # url: jdbc:mysql://localhost:3306/demo
    url: jdbc:h2:mem:testdb
    username: sa
    password: ''
    # driver-class-name: com.mysql.cj.jdbc.Driver
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
  devtools:
    restart:
      enabled: true
```

### **6.2. 엔티티(Entity) 정의**

- `@Entity`: JPA 엔티티 선언
- `@Id`: 기본 키(PK) 설정
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: 자동 증가 설정
- `@Column(nullable = false)`: 컬럼 제약 조건 추가

### **6.3. Repository 생성**

`JpaRepository`를 활용하면 데이터베이스 접근이 간편합니다.

### **6.4. H2 데이터베이스 실행 및 설정**

Spring Boot는 `H2 Database` 의존성을 추가하면 별도 설치 없이 내장(in-memory) 데이터베이스를 사용할 수 있습니다.

#### **6.4.1. H2 웹 콘솔(Web UI) 활성화**

H2의 웹 UI를 사용하려면 `application.yml`에서 다음 설정을 추가해야 합니다.

```yaml
spring:
  h2:
    console:
      enabled: true # H2 웹 콘솔 활성화
      path: /h2-console # 접속 경로 설정
```

#### **6.4.2. H2 콘솔 접속 방법**

애플리케이션을 실행한 후 브라우저에서 아래 URL로 접속하면 H2 콘솔을 사용할 수 있습니다.

```
http://localhost:8080/h2-console
```

##### **H2 콘솔 접속 정보**

- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: 없음 (`""`)

---

### **6.5. H2 데이터 유지 (파일 모드 설정)**

H2는 기본적으로 **메모리 모드(`mem:testdb`)**를 사용하기 때문에 **애플리케이션 종료 시 데이터가 사라집니다**.  
데이터를 유지하려면 **파일 모드**로 변경해야 합니다.

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/demo
    driver-class-name: org.h2.Driver
    username: sa
    password: ''
```

이렇게 설정하면 `./data/demo.mv.db` 파일이 생성되며, 애플리케이션을 재시작해도 데이터가 유지됩니다.

---

## **7. 트랜잭션 관리**

- `@Transactional`을 사용하여 트랜잭션을 관리할 수 있습니다.
- 트랜잭션 내에서 예외 발생 시 자동으로 롤백됩니다.

```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```

---

## **8. API 테스트**

### **8.1. 사용자 추가 (POST)**

```bash
curl -X POST "http://localhost:8080/users" \
     -H "Content-Type: application/json" \
     -d '{"name": "John", "email": "john@example.com"}'
```

### **8.2. 모든 사용자 조회 (GET)**

```bash
curl -X GET "http://localhost:8080/users"
```

### **8.3. 특정 사용자 조회 (GET)**

```bash
curl -X GET "http://localhost:8080/users/1"
```

### **8.4. 사용자 삭제 (DELETE)**

```bash
curl -X DELETE "http://localhost:8080/users/1"
```

---

## **9. 정리 및 추가 학습**

이제 스프링 부트의 기본적인 개념과 REST API, JPA를 이용한 데이터베이스 연동을 익혔습니다. 다음 단계에서는 **Spring Security, JWT 인증, 서비스 레이어 아키텍처 설계** 등을 학습할 수 있습니다.

### **추가 학습 추천**

1. **Spring Security를 이용한 인증 및 권한 관리**
2. **JWT(Json Web Token) 기반 로그인 구현**
3. **Swagger로 API 문서 자동 생성**
4. **Spring Boot + MySQL 연동**
5. **Redis를 활용한 캐싱과 세션 관리**

---

---

### **10.1. Spring Boot DevTools 자동 재시작 설정**

Spring Boot DevTools를 사용하면 **소스 코드 변경 시 애플리케이션이 자동으로 다시 시작**됩니다.  
그러나 기본적으로 `bootRun` 실행 시 자동 재시작이 활성화되지 않을 수도 있으므로 아래 설정을 확인해야 합니다.

#### **7.1.1. DevTools 설정 확인**

`application.yml`에서 DevTools 자동 재시작이 활성화되어 있는지 확인합니다.

```yaml
spring:
  devtools:
    restart:
      enabled: true
```

#### **7.1.2. Gradle `bootRun` 실행 시 DevTools 적용**

`build.gradle` 파일에서 `bootRun` 실행 시 DevTools가 활성화되도록 추가 설정을 합니다.

```gradle
bootRun {
    systemProperty "spring.devtools.restart.enabled", "true"
}
```

#### **7.1.3. 자동 빌드 및 재시작 활성화 (IntelliJ IDEA 설정)**

IntelliJ IDEA에서 **자동 빌드** 및 **DevTools 자동 재시작**이 활성화되어 있어야 합니다.

1. **File → Settings (Ctrl + Alt + S) → Build, Execution, Deployment → Compiler**

   - ✅ `Build project automatically` 활성화

2. **Advanced Settings (검색창에서 `Advanced` 입력)**

   - ✅ `Allow auto-make to start even if developed application is currently running` 체크

3. **Shift + Ctrl + A → `Registry` 검색 → `compiler.automake.allow.when.app.running` 활성화**

이제 소스 코드를 변경하면 Spring Boot DevTools가 자동으로 애플리케이션을 재시작합니다. 🚀
