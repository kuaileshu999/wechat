package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class User {
    @Id
    @Column(name = "phone")
    private String phone;

    @Column(name = "password")
    private String password;

    @Column(name = "students_name")
    private String name;

    // Constructors
    public User() {}

    // Getters and Setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
