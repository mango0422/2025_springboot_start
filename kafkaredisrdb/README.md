# 📌 **Kafka + Redis + MariaDB 연동 프로젝트 (Spring Boot 3.4.2)**

## **1️⃣ 프로젝트 개요**

이 프로젝트는 **Kafka, Redis, MariaDB** 를 사용하여 데이터를 송수신하고 처리하는 **메시징 시스템**을 구축하는 예제야.  
특히 **Kafka Streams**를 활용하여 특정 키워드가 포함된 메시지만 필터링하는 기능을 구현할 거야.

### **💡 주요 기능**

1. **Kafka Producer** → 메시지를 Kafka 토픽(`chat-topic`)으로 전송
2. **Kafka Streams** → 메시지를 필터링 (`"hello"`가 포함된 메시지만 `filtered-chat-topic`으로 이동)
3. **Kafka Consumer** → 필터링된 메시지를 Redis에 캐싱하고 MariaDB에 저장
4. **MariaDB** → 영구 저장소
5. **Redis** → 빠른 조회를 위한 캐싱

---

## **2️⃣ 프로젝트 구조**

```plaintext
kafkaredisrdb
├── HELP.md
├── build.gradle           # Gradle 설정 파일
├── docker-compose.yaml    # Kafka, Redis, MariaDB 설정
├── gradlew
├── gradlew.bat
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── kafkaredisrdb
    │   │               ├── config          # Kafka, Redis, JPA 설정
    │   │               ├── controller      # REST API 컨트롤러
    │   │               ├── kafka           # Kafka Producer & Consumer
    │   │               ├── model           # JPA 엔티티
    │   │               ├── repository      # JPA Repository
    │   │               ├── service         # 비즈니스 로직
    │   │               └── KafkaredisrdbApplication.java  # Main 클래스
    │   └── resources
    │       ├── application.properties
    │       ├── static
    │       └── templates
    └── test
        └── java
            └── com
                └── example
                    └── kafkaredisrdb
                        └── KafkaredisrdbApplicationTests.java
```

---

## **3️⃣ `docker-compose.yaml` (Kafka, Redis, MariaDB 실행)**

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    networks:
      - kafka_network

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - '9092:9092'
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    networks:
      - kafka_network

  redis:
    image: redis:latest
    container_name: redis
    ports:
      - '6379:6379'
    networks:
      - kafka_network

  mariadb:
    image: mariadb:latest
    container_name: mariadb
    ports:
      - '3306:3306'
    environment:
      MARIADB_ROOT_PASSWORD: root
      MARIADB_DATABASE: mydb
      MARIADB_USER: user
      MARIADB_PASSWORD: password
    networks:
      - kafka_network

networks:
  kafka_network:
    driver: bridge
```

🚀 **명령어로 컨테이너 실행**

```sh
docker-compose up -d
```

---

## **4️⃣ `build.gradle` (의존성)**

```gradle
dependencies {
    // ✅ Spring Boot Web (REST API)
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // ✅ Kafka (Producer & Consumer)
    implementation 'org.springframework.kafka:spring-kafka'

    // ✅ Kafka Streams (데이터 필터링)
    implementation 'org.apache.kafka:kafka-streams'

    // ✅ Redis (캐싱)
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // ✅ MariaDB 드라이버
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'

    // ✅ JPA (데이터 저장)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // ✅ Lombok (자동 코드 생성)
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // ✅ Spring Configuration Processor
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'

    // ✅ Kafka 테스트
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## **5️⃣ `application.properties`**

```properties
server.port=8080

# MariaDB 설정
spring.datasource.url=jdbc:mariadb://mariadb:3306/mydb
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Redis 설정
spring.data.redis.host=redis
spring.data.redis.port=6379

# Kafka 설정
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=chat-group
spring.kafka.consumer.auto-offset-reset=earliest
```

---

## **6️⃣ Kafka Streams 설정 (`KafkaStreamsConfig.java`)**

```java
package com.example.kafkaredisrdb.config;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaStreamsConfig {

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream("chat-topic");

        // "hello"가 포함된 메시지만 필터링 후 "filtered-chat-topic"에 저장
        stream.filter((key, value) -> value.contains("hello"))
              .to("filtered-chat-topic");

        return stream;
    }
}
```

---

## **7️⃣ Kafka Producer (`KafkaProducer.java`)**

```java
package com.example.kafkaredisrdb.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        kafkaTemplate.send("chat-topic", message);
    }
}
```

---

## **8️⃣ Kafka Consumer (`KafkaConsumer.java`)**

```java
package com.example.kafkaredisrdb.kafka;

import com.example.kafkaredisrdb.model.User;
import com.example.kafkaredisrdb.repository.UserRepository;
import com.example.kafkaredisrdb.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final UserRepository userRepository;
    private final RedisService redisService;

    @KafkaListener(topics = "filtered-chat-topic", groupId = "chat-group")
    public void listen(String message) {
        System.out.println("Received Message: " + message);

        User user = new User();
        user.setName(message);
        userRepository.save(user);

        redisService.cacheUser(user);
    }
}
```

---

## 🚀 **실행 방법**

```sh
# 1. Docker 컨테이너 실행
docker-compose up -d

# 2. Spring Boot 실행
./gradlew bootRun

# 3. 메시지 전송
curl -X POST "http://localhost:8080/api/send?message=hello"
```

이제 Kafka가 메시지를 받고, `"hello"`가 포함된 메시지만 **Kafka Streams**를 통해 필터링 후 저장되는 걸 확인할 수 있어.  
추가 질문 있으면 편하게 물어봐! 🚀🔥
