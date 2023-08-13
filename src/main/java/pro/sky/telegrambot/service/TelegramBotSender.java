package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.Task;
import pro.sky.telegrambot.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TelegramBotSender {
    //первый метод посылает боту через execute сообщение
    //второй метод по расписанию сканирует базу и тоже посылает боту сообщение, если надо
    @Autowired
    private TelegramBot telegramBot; //вызываем только execute

    @Autowired
    private TaskRepository repository;

    private final Logger logger = LoggerFactory.getLogger(TelegramBotSender.class);

    public void sendMessage(Long chatId, String messageText) {
        SendMessage message = new SendMessage(chatId, messageText);
        logger.info("Sending message: chatId = {}, text = {}", chatId, messageText);
        SendResponse response = telegramBot.execute(message);
        if (response.isOk()) return;
        logger.info("SendingError {}, {}", response.errorCode(), response.description());
    }
    @Scheduled(fixedDelay = 60_000L)  //каждые 60 сек
    public void executeTasks() {
        List<Task> list = repository.findByPlanTimeLessThanAndFactTimeIsNull(LocalDateTime.now());
        list.forEach(t->{
            sendMessage(t.getChatId(),t.getMessage());
            t.setFactTime(LocalDateTime.now());  //проставляем отметку о выполнении
            repository.save(t);
        });
    }
}
