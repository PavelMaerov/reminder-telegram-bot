package pro.sky.telegrambot;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class TelegramBotSenderTest {
    @Mock
    private TelegramBot bot;
    @Mock
    private TaskRepository repository; //нужен, чтобы ставить отметку о выполнении factTime

    @InjectMocks
    TelegramBotSender out; //ObjectUnderTest

    //фактически в этом сервисе надо протестировать 2 метода sendMessage и executeTasks
    @Test
    public void testSendMessage() {
        SendResponse response = BotUtils.fromJson("{\"ok\":true}", SendResponse.class);
        //метод execute замоканного bot должен возвращать результат - SendResponse. Подставляем результат
        Mockito.when(bot.execute(any())).thenReturn(response);
        //инициируем взаимодействие с моком bot
        out.sendMessage(321L,"Test");
        //проверяем результат взаимодействия - переданные в execute параметры
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(bot).execute(sendMessageCaptor.capture());
        SendMessage actualSendMessage = sendMessageCaptor.getValue();

        assertEquals(321L, actualSendMessage.getParameters().get("chat_id"));
        assertEquals("Test", actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void testExecuteTasks() {
        SendResponse response = BotUtils.fromJson("{\"ok\":true}", SendResponse.class);
        Mockito.when(bot.execute(any())).thenReturn(response);

        //единственная задача в составе возвращаемой из репозитория коллекции
        Task task = new Task();
        task.setId(123);
        task.setChatId(321);
        task.setPlanTime(LocalDateTime.of(2022, 1, 1, 20,0));
        //factTime - пусто
        task.setMessage("Test");

        Mockito.when(repository.findByPlanTimeLessThanAndFactTimeIsNull(any()))
               .thenReturn(Collections.singletonList(task));

        //инициируем взаимодействие с моками
        out.executeTasks();
        //проверяем результат взаимодействия - переданные в bot.execute параметры
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(bot).execute(sendMessageCaptor.capture());
        SendMessage actualSendMessage = sendMessageCaptor.getValue();

        assertEquals(321L, actualSendMessage.getParameters().get("chat_id"));
        assertEquals("Test", actualSendMessage.getParameters().get("text"));

        //Проверяем переданные в repository.save параметры
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        Mockito.verify(repository).save(taskCaptor.capture());
        Task actualTask = taskCaptor.getValue();
        //проверяем поля объекта, отправленного в репозиторий
        //Если бы не поле factTime (фактическое время исполнения задачи, взятое с часов)
        //то можно было бы сравнить объекты task через equals
        assertEquals(123, actualTask.getId());
        assertEquals(321, actualTask.getChatId());
        assertEquals(0, ChronoUnit.SECONDS.between(actualTask.getFactTime(), LocalDateTime.now()));
        assertEquals(LocalDateTime.of(2022, 1, 1, 20,0), actualTask.getPlanTime());
        assertNull(actualTask.getGetTime()); //null - т.к. не задали в task
        assertEquals("Test", actualTask.getMessage());

    }


    //Mockito.when(repository.save(any())).thenReturn(task);
    //Mockito.when(repository.findByPlanTimeLessThanAndFactTimeIsNull(any()))
    //        .thenReturn(Collections.singletonList(task));
    //sender.executeTasks();
    //assertNotNull(bot);

}
