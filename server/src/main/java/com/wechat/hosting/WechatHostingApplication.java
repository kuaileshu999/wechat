package com.wechat.hosting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WechatHostingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WechatHostingApplication.class, args);
    }
}
