package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Task;
import pro.sky.telegrambot.repository.TaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    //внедряем класс бот, чтобы заявить о себе после создания и он дергал наш метод process.
    //методы бота мы не вызываем
    private TelegramBot telegramBot;
    @Autowired
    private TaskRepository repository;
    @Autowired
    //внедряем посылателя, чтобы отправлять ответы боту на updates
    private TelegramBotSender sender;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            //у update может не быть message и text, сказал наставник
            Message message = update.message();
            if (message != null) {
                Long chatId=update.message().chat().id();
                String text = message.text();
                if (text != null) {
                    if (text.equals("/start")) {
                        sender.sendMessage(chatId, "Привет!");
                    }else if(correctUpdate(text, chatId)){
                        sender.sendMessage(chatId, "Задание принято");
                    } else {
                        sender.sendMessage(chatId, "Задание не соответствует формату - 01.01.2022 20:00 Сделать домашнюю работу");
                    }
                } else {
                    sender.sendMessage(chatId, "Нет текста в сообщении. Принимаю задания по формату - 01.01.2022 20:00 Сделать домашнюю работу");
                }
            } else {
                logger.info("No message");  //Раз нет сообщения, то нет и chatID и нельзя ответить
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private boolean correctUpdate(String text, long chatId) {
        //проверяет строку на корректность
        //Если корректная - сохраняет в репозитории
        //Ожидаем формат - 01.01.2022 20:00 Сделать домашнюю работу
        Pattern pattern=Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)(.+)");
        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches()) return false;
        LocalDateTime planTime;
        try {
            //секунд в задании быть не может, отсекаются шаблоном
            planTime = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } catch(DateTimeParseException e) {
            return false;
        }
        Task task = new Task(); //не стал делать конструктор с параметрами
        task.setChatId(chatId);
        task.setGetTime(LocalDateTime.now());
        task.setPlanTime(planTime);
        task.setMessage(matcher.group(3));
        repository.save(task);
        return true;
    }
}
