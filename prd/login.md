// 文件：UserController.java
package com.example.demo.controller;

import com.example.demo.entity.Student;
import com.example.demo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private StudentService studentService;
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest) {
        String phone = loginRequest.getPhone();
        String password = loginRequest.getPassword();
    
        Student student = studentService.login(phone, password);
        Map<String, Object> response = new HashMap<>();
    
        if (student == null) {
            response.put("message", "账号或者密码错误");
            response.put("success", false);
        } else {
            response.put("message", "登录成功");
            response.put("success", true);
            response.put("userId", student.getStudentId());
        }
        return response;
    }
    
    // 内部类，用于接收登录请求参数
    static class LoginRequest {
        private String phone;
        private String password;
    
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

}
