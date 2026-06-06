package com.wechat.hosting.task;

import com.wechat.hosting.repository.MessageRepository;
import com.wechat.hosting.service.HostingConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final MessageRepository messageRepository;
    private final HostingConfigService hostingConfigService;

    public ScheduledTasks(MessageRepository messageRepository, HostingConfigService hostingConfigService) {
        this.messageRepository = messageRepository;
        this.hostingConfigService = hostingConfigService;
    }

    @Scheduled(cron = "0 30 3 * * ?")
    @Transactional
    public void cleanupOldMessages() {
        LocalDateTime before = LocalDateTime.now().minusMonths(1);
        int deleted = messageRepository.deleteBySentAtBefore(before);
        log.info("Cleaned up {} messages older than {}", deleted, before);
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void activateScheduledHostingConfigs() {
        hostingConfigService.processScheduledConfigs();
    }
}
