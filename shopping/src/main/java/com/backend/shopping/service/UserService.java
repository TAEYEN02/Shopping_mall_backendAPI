package com.backend.shopping.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.shopping.controller.UserController.UpdateProfileRequest;
import com.backend.shopping.entity.User;
import com.backend.shopping.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 이름으로 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    /**
     * 사용자 프로필 업데이트
     */
    public User updateProfile(String username, UpdateProfileRequest request) {
        User user = getUserByUsername(username);
        
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        
        return userRepository.save(user);
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(String username) {
        User user = getUserByUsername(username);
        userRepository.delete(user);
    }
}
