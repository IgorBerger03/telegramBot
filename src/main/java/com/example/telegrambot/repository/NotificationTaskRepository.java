package com.example.telegrambot.repository;

import com.example.telegrambot.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository <NotificationTask, Long> {

    List<NotificationTask> findAllByNotificationDateTime(LocalDateTime dateTime);
}
