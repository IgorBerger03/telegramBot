package com.example.telegrambot.listener;

import com.example.telegrambot.component.SendHelper;
import com.example.telegrambot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Pattern PATTERN = Pattern.compile("([0-9.:\\s]{16})(\\s)(\\W+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final SendHelper sendHelper;


    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService,
                                      SendHelper sendHelper) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.sendHelper = sendHelper;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
        updates.forEach(update -> {
            String text = update.message().text();
            Long chatId = update.message().chat().id();
            if("/start".equals(text)){
                sendHelper.sendMessage(chatId, "Для планирования задачи отправьте её в формате:" +
                        " \n**01.01.2022 20:00 Сделать домашнюю работу**", ParseMode.MarkdownV2);
            } else {
                Matcher matcher = PATTERN.matcher(text);
                LocalDateTime dateTime;
                if(matcher.find() && (dateTime = parse(matcher.group(1))) != null) {
                    String message = matcher.group(2);
                    notificationTaskService.create(chatId, message, dateTime);
                    sendHelper.sendMessage(chatId, "Задача запланирована!");
                } else {
                    sendHelper.sendMessage(chatId, "Некорректный формат сообщения!");
                }
            }
        });
    } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    @Nullable
    private LocalDateTime parse(String dateTime){
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch(DateTimeParseException e) {
            return null;
        }
    }
}
