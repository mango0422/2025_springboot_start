# **스프링 부트와 데이터베이스 연동 방법**

스프링 부트에서 데이터베이스와 상호작용하는 방법에는 여러 가지가 있어. 대표적인 방법은 다음과 같아:

1. **SQL (Structured Query Language)**: SQL을 직접 사용하여 데이터베이스를 조작.
2. **JDBC (Java Database Connectivity)**: Java의 표준 데이터베이스 연결 방식.
3. **JPA (Java Persistence API)**: ORM(Object Relational Mapping) 기반으로 SQL 없이 객체 중심으로 DB 연동.
4. **MyBatis**: SQL을 직접 작성하면서도 객체 매핑 기능을 제공하는 프레임워크.

각각의 방식이 무엇인지, 언제 사용하면 좋은지, 실제 코드와 함께 설명해볼게.

---

## **1. SQL (Structured Query Language)**

### **1.1. SQL이란?**

SQL은 관계형 데이터베이스(RDBMS)에서 데이터를 **추가(CREATE), 조회(SELECT), 수정(UPDATE), 삭제(DELETE)** 하는데 사용되는 표준 언어야.

### **1.2. 기본 SQL 문법**

```sql
-- 1. 테이블 생성
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

-- 2. 데이터 삽입
INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');

-- 3. 데이터 조회
SELECT * FROM users;

-- 4. 특정 데이터 조회
SELECT * FROM users WHERE email = 'alice@example.com';

-- 5. 데이터 수정
UPDATE users SET name = 'Alice Updated' WHERE email = 'alice@example.com';

-- 6. 데이터 삭제
DELETE FROM users WHERE email = 'alice@example.com';
```

### **SQL을 직접 사용해야 하는 경우**

✅ **장점**

- SQL의 강력한 기능(Join, Group By, Subquery 등)을 자유롭게 활용할 수 있음.
- 성능 튜닝이 필요한 경우 직접 최적화 가능.

❌ **단점**

- SQL을 직접 관리해야 해서 **코드가 복잡**해질 수 있음.
- Java 코드와 SQL이 섞이면 **유지보수가 어려워짐**.

---

## **2. JDBC (Java Database Connectivity)**

### **2.1. JDBC란?**

JDBC는 **자바에서 데이터베이스와 연결하고 SQL을 실행하기 위한 표준 API**야. SQL을 직접 다룰 수 있어서 강력하지만 코드가 많아지는 단점이 있어.

### **2.2. JDBC 데이터베이스 연결 예제**

```java
package com.example.demo.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcExample {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/testdb";
        String user = "root";
        String password = "password";

        try {
            // 1. 데이터베이스 연결
            Connection conn = DriverManager.getConnection(url, user, password);

            // 2. SQL 실행
            String sql = "SELECT * FROM users";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            // 3. 결과 출력
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id") + ", Name: " + rs.getString("name"));
            }

            // 4. 연결 해제
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### **JDBC를 사용해야 하는 경우**

✅ **장점**

- SQL을 직접 다룰 수 있어 **세부적인 제어** 가능.
- 데이터베이스 벤더(Oracle, MySQL 등)에 최적화된 SQL을 작성 가능.

❌ **단점**

- 데이터베이스 연결, SQL 실행, 결과 처리 등의 **반복적인 코드가 많음**.
- **트랜잭션 관리가 어렵고**, 유지보수가 힘듦.

---

## **3. JPA (Java Persistence API)**

### **3.1. JPA란?**

JPA는 **SQL을 직접 작성하지 않고, 객체 중심으로 데이터베이스와 상호작용할 수 있도록 해주는 ORM 기술**이야.

JPA는 **Spring Data JPA**와 함께 사용하면 자동으로 SQL을 생성해줘서 코드가 깔끔해지고 생산성이 높아져.

### **3.2. JPA 엔티티(Entity) 클래스**

```java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;
}
```

### **3.3. JPA Repository (데이터베이스 접근)**

```java
package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
```

### **3.4. JPA 컨트롤러**

```java
package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}
```

### **JPA를 사용해야 하는 경우**

✅ **장점**

- SQL을 직접 작성할 필요가 없어 코드가 깔끔해짐.
- **객체 중심 개발 가능** (객체 지향적인 코드 작성).
- **트랜잭션 자동 관리**, 유지보수가 편리함.

❌ **단점**

- 내부적으로 SQL을 자동 생성하므로 **복잡한 쿼리 최적화가 어려울 수 있음**.
- SQL 직접 제어가 어렵기 때문에 **성능 튜닝이 필요할 수 있음**.

---

## **4. MyBatis**

### **4.1. MyBatis란?**

MyBatis는 SQL을 직접 작성하면서도, **SQL과 Java 객체를 쉽게 매핑할 수 있도록 도와주는 프레임워크**야.

### **4.2. MyBatis 설정**

`application.yml`

```yaml
mybatis:
  mapper-locations: classpath:mappers/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

### **4.3. MyBatis Mapper**

```java
package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User getUserById(Long id);

    @Insert("INSERT INTO users(name, email) VALUES(#{name}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);
}
```

### **MyBatis를 사용해야 하는 경우**

✅ **장점**

- **SQL을 직접 제어**할 수 있어 복잡한 쿼리에 적합.
- JDBC보다 코드가 짧고, **객체 매핑이 자동화**됨.

❌ **단점**

- SQL을 직접 관리해야 해서 유지보수가 어려울 수 있음.

---

# **최종 비교**

| 방식               | SQL | JDBC | JPA | MyBatis |
| ------------------ | --- | ---- | --- | ------- |
| SQL 직접 작성      | O   | O    | X   | O       |
| 자동화 지원        | X   | X    | O   | X       |
| 객체 중심 개발     | X   | X    | O   | X       |
| 트랜잭션 자동 관리 | X   | X    | O   | X       |

---

## **정리**

- **빠르게 개발하려면 JPA 사용**
- **SQL 최적화가 필요하면 MyBatis 사용**
- **완전한 제어가 필요하면 JDBC 사용**
- **특정 성능 최적화가 필요하면 SQL 직접 작성**

---

이번에는 **아예 처음부터 3가지(DB 직접 접근(SQL), JDBC, JPA, MyBatis)**를 **하나의 프로젝트에서 동시에 사용하려면 어떻게 설정해야 하는지**, 그리고 **각 방식이 어떻게 다르고 어떤 설정이 필요한지**까지 모두 다뤄볼게.

---

# 1. 프로젝트 의존성 설정 (Gradle)

스프링 부트에서 **SQL, JDBC, JPA, MyBatis**를 동시에 사용하려면 `build.gradle`에서 모든 의존성을 추가해야 해.

## 1.1. `build.gradle` (Gradle 기반)

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web' // 웹 기능

    // 1. JDBC (SQL 직접 실행)
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'

    // 2. JPA (ORM)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.hibernate.orm:hibernate-core:6.3.1.Final'

    // 3. MyBatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'

    // H2 (테스트용) & MySQL (실제 DB)
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'mysql:mysql-connector-java'

    // Lombok (코드 간소화)
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // 테스트 라이브러리
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

# **2. `application.yml` 설정**

스프링 부트에서 **MySQL과 H2를 동시에 사용**할 수 있도록 설정할 거야.

## 2.1. 기본 설정

```yaml
spring:
  datasource:
    primary: # MySQL (JPA + JDBC에서 사용)
      url: jdbc:mysql://localhost:3306/primary_db
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: root
      password: password

    secondary: # H2 (MyBatis에서 사용)
      url: jdbc:h2:mem:secondary_db
      driver-class-name: org.h2.Driver
      username: sa
      password: ''

  jpa:
    primary:
      database-platform: org.hibernate.dialect.MySQL8Dialect
      hibernate:
        ddl-auto: update # create, update, validate, none
      show-sql: true

  mybatis:
    configuration:
      map-underscore-to-camel-case: true
    mapper-locations: classpath:mappers/*.xml
```

### **설정 설명**

- **MySQL은 `primary` 데이터베이스**로 지정 (JPA, JDBC에서 사용)
- **H2는 `secondary` 데이터베이스**로 지정 (MyBatis에서 사용)
- **JPA의 Hibernate 설정 추가**
  - `ddl-auto: update`: **테이블이 자동 생성되도록 설정**
  - `show-sql: true`: **SQL 실행 로그를 출력**
- **MyBatis는 XML 기반 매퍼를 사용하도록 설정** (`mappers/*.xml`)

---

# **3. 3가지 방식(JDBC, JPA, MyBatis) 동시에 사용할 때 문제점**

3가지를 한 프로젝트에서 동시에 사용할 때 몇 가지 주의해야 할 점이 있어.

## 3.1. 문제점

✅ **1) 트랜잭션 문제**

- JPA는 내부적으로 **트랜잭션을 자동으로 관리**하지만, JDBC와 MyBatis는 수동으로 트랜잭션을 관리해야 할 수도 있어.
- **해결 방법:**
  - JPA와 JDBC, MyBatis의 트랜잭션을 통일하려면 `@Transactional`을 적절히 사용해야 함.
  - 각 DB별 `TransactionManager`를 따로 지정할 수도 있음.

✅ **2) 데이터베이스 다중 연결 문제**

- `@Primary`를 사용하지 않으면 스프링 부트가 **어떤 DB를 사용할지 모를 수 있음**.
- **해결 방법:**
  - `@Primary` 어노테이션을 추가하여 **기본 데이터 소스를 명확히 지정**해야 함.

✅ **3) SQL 실행 방식 차이**

- JPA는 SQL을 자동으로 생성하지만, JDBC와 MyBatis는 직접 작성해야 함.
- 같은 `User` 엔티티를 사용할 경우, **테이블 생성 및 조회 방식이 다르기 때문에 충돌이 날 수 있음**.
- **해결 방법:**
  - `JPA Entity`와 `MyBatis DTO`를 분리하여 설계하는 것이 좋음.

✅ **4) 테이블 자동 생성 문제**

- JPA는 `ddl-auto: update` 설정 시 **테이블을 자동 생성**하지만, MyBatis와 JDBC는 SQL을 직접 실행해야 함.
- **해결 방법:**
  - MyBatis 및 JDBC는 `schema.sql`을 이용하여 테이블을 명시적으로 생성.

---

# **4. 실제 구현 코드**

## **4.1. JDBC 방식 (SQL 직접 실행)**

```java
package com.example.demo.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findAllUsers() {
        return jdbcTemplate.queryForList("SELECT * FROM users");
    }

    public void insertUser(String name, String email) {
        jdbcTemplate.update("INSERT INTO users (name, email) VALUES (?, ?)", name, email);
    }
}
```

---

## **4.2. JPA 방식 (ORM)**

```java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;
}
```

```java
package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
```

---

## **4.3. MyBatis 방식**

### **Mapper 인터페이스**

```java
package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users")
    List<User> getAllUsers();

    @Insert("INSERT INTO users(name, email) VALUES(#{name}, #{email})")
    void insertUser(User user);
}
```

### **XML Mapper**

`resources/mappers/UserMapper.xml`

```xml
<mapper namespace="com.example.demo.mapper.UserMapper">
    <select id="getAllUsers" resultType="com.example.demo.entity.User">
        SELECT * FROM users
    </select>

    <insert id="insertUser">
        INSERT INTO users(name, email) VALUES(#{name}, #{email})
    </insert>
</mapper>
```

---

# **5. 3가지 방식(JDBC, JPA, MyBatis) 컨트롤러에서 호출**

```java
package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.jdbc.UserJdbcRepository;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserJdbcRepository userJdbcRepository;
    private final UserMapper userMapper;

    public UserController(UserRepository userRepository, UserJdbcRepository userJdbcRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userJdbcRepository = userJdbcRepository;
        this.userMapper = userMapper;
    }

    @GetMapping("/jpa")
    public List<User> getUsersByJPA() {
        return userRepository.findAll();
    }

    @GetMapping("/jdbc")
    public List<Map<String, Object>> getUsersByJDBC() {
        return userJdbcRepository.findAllUsers();
    }

    @GetMapping("/mybatis")
    public List<User> getUsersByMyBatis() {
        return userMapper.getAllUsers();
    }
}
```

---

이제 **JDBC, JPA, MyBatis**를 **한 프로젝트에서 동시에 사용할 수 있도록 설정하고 구현하는 방법**을 제대로 이해했을 거야.  
더 궁금한 점이 있으면 질문해줘! 🚀
