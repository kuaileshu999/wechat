package com.wechat.hosting;

import com.wechat.hosting.service.TransferLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransferLogServiceTest {

    @Autowired
    TransferLogService transferLogService;

    @Test
    void listRecentShouldNotThrow() {
        transferLogService.listRecent().forEach(System.out::println);
    }
}
