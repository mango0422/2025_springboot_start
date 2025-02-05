package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 (MySQL, H2)
    private Long id;

    @Column(nullable = false, length = 50) // 필수 입력 & 최대 길이 50
    private String name;

    @Column(unique = true, nullable = false) // 유니크 제약 조건 추가
    private String email;
}
