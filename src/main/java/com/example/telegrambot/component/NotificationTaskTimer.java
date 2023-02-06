package com.example.telegrambot.component;

import com.example.telegrambot.repository.NotificationTaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationTaskTimer {
    private final NotificationTaskRepository notificationTaskRepository;
    private final SendHelper sendHelper;

    public NotificationTaskTimer(NotificationTaskRepository notificationTaskRepository, SendHelper sendHelper) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.sendHelper = sendHelper;
    }
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void task(){
        notificationTaskRepository.findAllByNotificationDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        ).forEach(notificationTask -> {
            sendHelper.sendMessage(notificationTask.getUserId(), notificationTask.getMessage());
            notificationTaskRepository.delete(notificationTask);
        });

    }
}
