package com.example.demo.controller;

import com.example.demo.model.LoginRequest;
import com.example.demo.model.LoginResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByPhone(request.getPhone());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(request.getPassword())) {
                return new LoginResponse(true, "你好");
            }
        }
        
        return new LoginResponse(false, "账号密码错误");
    }
}
