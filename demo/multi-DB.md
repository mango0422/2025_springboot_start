## **1. 스프링 부트 3.4.1에서 여러 데이터베이스를 사용하는 방법**

스프링 부트 3.4.1에서는 **하나의 프로젝트에서 여러 개의 데이터베이스를 동시에 사용할 수 있음**. 따라서 별도의 프로젝트를 만들지 않고도 **하나의 스프링 부트 애플리케이션 내에서 여러 데이터베이스를 동시에 접근할 수 있어**.

이를 위해 **방법론(이론)과 실제 구현 방식**을 설명할게.

---

# **2. 방법론 (이론)**

하나의 프로젝트에서 여러 개의 데이터베이스를 접근하려면 다음과 같은 방법이 있어:

### **1) 다중 데이터 소스 (Multiple Data Sources) 설정**

- **두 개 이상의 `DataSource`를 설정**하고, 각각에 대한 **JPA 설정을 따로 관리**하는 방식.
- **주 데이터베이스(Primary)**와 **보조 데이터베이스(Secondary)**를 설정.
- 서로 다른 `EntityManagerFactory`와 `TransactionManager`를 사용하여 **각 데이터베이스에 독립적인 트랜잭션을 적용**할 수 있음.

### **2) `RoutingDataSource` 사용**

- 동적으로 **데이터베이스를 선택할 필요가 있을 때** 활용.
- **멀티 테넌시(Multi-Tenancy)** 아키텍처를 적용할 때 유용.

---

# **3. 실제 구현 방식**

여기서는 **H2 데이터베이스**와 **MySQL 데이터베이스**를 동시에 접근하는 예제를 구현할 거야.

## **3.1. 프로젝트 설정 (`application.yml`)**

먼저, `application.yml`에서 두 개의 데이터베이스 설정을 추가하자.

```yaml
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/primary_db
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: root
      password: password
    secondary:
      url: jdbc:h2:mem:secondary_db
      driver-class-name: org.h2.Driver
      username: sa
      password: ''

  jpa:
    primary:
      database-platform: org.hibernate.dialect.MySQL8Dialect
      hibernate:
        ddl-auto: update
      show-sql: true

    secondary:
      database-platform: org.hibernate.dialect.H2Dialect
      hibernate:
        ddl-auto: update
      show-sql: true
```

---

## **3.2. `PrimaryDataSource` (MySQL) 설정**

먼저 **MySQL을 위한 `DataSource`** 설정을 한다.

```java
package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo.repository.primary",
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            @Qualifier("primaryDataSource") DataSource primaryDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(primaryDataSource);
        em.setPackagesToScan("com.example.demo.entity.primary");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public JpaTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
    }
}
```

---

## **3.3. `SecondaryDataSource` (H2) 설정**

이제 **H2 데이터베이스 설정**을 추가한다.

```java
package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo.repository.secondary",
        entityManagerFactoryRef = "secondaryEntityManagerFactory",
        transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSourceProperties secondaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource secondaryDataSource() {
        return secondaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(secondaryDataSource);
        em.setPackagesToScan("com.example.demo.entity.secondary");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public JpaTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory) {
        return new JpaTransactionManager(secondaryEntityManagerFactory.getObject());
    }
}
```

---

## **3.4. 엔티티 클래스 정의**

### **MySQL에 저장할 엔티티**

```java
package com.example.demo.entity.primary;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "primary_users")
public class PrimaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
```

### **H2에 저장할 엔티티**

```java
package com.example.demo.entity.secondary;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "secondary_users")
public class SecondaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
}
```

---

## **3.5. Repository 설정**

### **Primary DB Repository**

```java
package com.example.demo.repository.primary;

import com.example.demo.entity.primary.PrimaryUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrimaryUserRepository extends JpaRepository<PrimaryUser, Long> {
}
```

### **Secondary DB Repository**

```java
package com.example.demo.repository.secondary;

import com.example.demo.entity.secondary.SecondaryUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecondaryUserRepository extends JpaRepository<SecondaryUser, Long> {
}
```

---

## **3.6. 컨트롤러 구현**

```java
package com.example.demo.controller;

import com.example.demo.entity.primary.PrimaryUser;
import com.example.demo.entity.secondary.SecondaryUser;
import com.example.demo.repository.primary.PrimaryUserRepository;
import com.example.demo.repository.secondary.SecondaryUserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final PrimaryUserRepository primaryRepo;
    private final SecondaryUserRepository secondaryRepo;

    public UserController(PrimaryUserRepository primaryRepo, SecondaryUserRepository secondaryRepo) {
        this.primaryRepo = primaryRepo;
        this.secondaryRepo = secondaryRepo;
    }

    @GetMapping("/primary")
    public List<PrimaryUser> getPrimaryUsers() {
        return primaryRepo.findAll();
    }

    @GetMapping("/secondary")
    public List<SecondaryUser> getSecondaryUsers() {
        return secondaryRepo.findAll();
    }
}
```

---

이렇게 하면 **하나의 스프링 부트 프로젝트에서 두 개의 DB를 동시에 접근할 수 있음**. 🚀
