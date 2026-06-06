package com.wechat.hosting.service;

import com.wechat.hosting.dto.LoginRequest;
import com.wechat.hosting.dto.LoginVO;
import com.wechat.hosting.entity.SysUser;
import com.wechat.hosting.repository.SysUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    @Transactional
    public LoginVO login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));
        if (user.getStatus() != 1) {
            throw new IllegalArgumentException("账号已禁用");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        user.setLastLoginAt(LocalDateTime.now());
        sysUserRepository.save(user);
        return new LoginVO(user.getId(), user.getUsername(), user.getRealName(), user.getRole(), user.getAvatarUrl());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
