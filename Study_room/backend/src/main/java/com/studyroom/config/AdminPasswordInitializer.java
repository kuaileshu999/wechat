package com.studyroom.config;

import com.studyroom.entity.SysUser;
import com.studyroom.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminPasswordInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresent(user -> {
            if (!passwordEncoder.matches("Admin@123", user.getPassword())) {
                user.setPassword(passwordEncoder.encode("Admin@123"));
                user.setLocked(0);
                user.setLoginFailCount(0);
                userRepository.save(user);
            }
        });
    }
}
