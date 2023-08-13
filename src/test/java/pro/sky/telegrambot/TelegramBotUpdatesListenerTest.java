package pro.sky.telegrambot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.Task;
import pro.sky.telegrambot.repository.TaskRepository;
import pro.sky.telegrambot.service.TelegramBotSender;
import pro.sky.telegrambot.service.TelegramBotUpdatesListener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTest {

    @Mock
    private TaskRepository repository;
    @Mock
    private TelegramBotSender sender;
    @InjectMocks
    private TelegramBotUpdatesListener out;  //ObjectUnderTest

    //фактически в этом сервисе надо протестировать единственный метод process

    private void checkArgumentOfSender(long expectedChatId, String expectedText) {
        //вызывается после обращения к замоканному sender из out.process
        //для проверки аргументов вызова sendMessage
        //у меня 4 разных случая, поэтому сделал отдельный метод
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sender).sendMessage(longCaptor.capture(), stringCaptor.capture());
        Long actualChatId = longCaptor.getValue();
        String actualText = stringCaptor.getValue();

        assertEquals(expectedChatId, actualChatId);
        assertEquals(expectedText, actualText);
    }

    @Test
    public void testProcessNormalUpdate(){
        //Методы замоканных объектов sender.sendMessage и repository.save
        //у реальных объектов все равно ничего не возвращают
        //далать when этих методов не нужно
        //а нужно проверить, какие параметры попадают им на вход
        Update update = BotUtils.fromJson(
                "{\"message\":{\"chat\":{\"id\":321},\"text\":\"01.01.2022 20:00 !!!\"}}"
                ,Update.class);
        //Инициируем взаимодействие с моками. Мокито все запоминает.
        out.process(Collections.singletonList(update));
        //Проверяем результат взаимодействия c sender
        checkArgumentOfSender(321, "Задание принято");
        //Проверяем результат взаимодействия c repository
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        Mockito.verify(repository).save(taskCaptor.capture());
        Task task = taskCaptor.getValue();
        //проверяем поля объекта, отправленного в репозиторий
        assertEquals(0, task.getId());
        assertEquals(321, task.getChatId());
        assertEquals(0, ChronoUnit.SECONDS.between(task.getGetTime(),LocalDateTime.now()));
        assertEquals(LocalDateTime.of(2022, 1, 1, 20,0), task.getPlanTime());
        assertNull(task.getFactTime());
        assertEquals("!!!", task.getMessage());
    }
    @Test
    public void testProcessUpdateWithoutMessage(){
        Update update = BotUtils.fromJson(
                "{\"message\":null}"
                ,Update.class);
        out.process(Collections.singletonList(update));
        //замоканные объекты не вызывались
        Mockito.verify(sender,times(0)).sendMessage(any(), any());
        Mockito.verify(repository,times(0)).save(any());
    }
    @Test
    public void testProcessUpdateWithoutText(){
        Update update = BotUtils.fromJson(
                "{\"message\":{\"chat\":{\"id\":321}}}"  //текста нет
                ,Update.class);
        out.process(Collections.singletonList(update));
        checkArgumentOfSender(321, "Нет текста в сообщении. Принимаю задания по формату - 01.01.2022 20:00 Сделать домашнюю работу");
        Mockito.verify(repository,times(0)).save(any());
    }
    @Test
    public void testProcessIncorrectUpdate(){
        Update update = BotUtils.fromJson(
                "{\"message\":{\"chat\":{\"id\":321},\"text\":\"*01.01.2022 20:00 !!!\"}}"
                ,Update.class);
        out.process(Collections.singletonList(update));
        checkArgumentOfSender(321, "Задание не соответствует формату - 01.01.2022 20:00 Сделать домашнюю работу");
        Mockito.verify(repository,times(0)).save(any());
    }
    @Test
    public void testProcessStartUpdate(){
        Update update = BotUtils.fromJson(
                "{\"message\":{\"chat\":{\"id\":321},\"text\":\"/start\"}}"
                ,Update.class);
        out.process(Collections.singletonList(update));
        checkArgumentOfSender(321, "Привет!");
        Mockito.verify(repository,times(0)).save(any());
    }
}
