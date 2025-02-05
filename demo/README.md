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
