package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional // @Transactional을 사용하면 해당 메서드 실행 중 예외 발생 시 자동으로 롤백됨.
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
