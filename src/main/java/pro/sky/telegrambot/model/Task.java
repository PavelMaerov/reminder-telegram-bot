package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Task {
    @GeneratedValue
    @Id
    private int id;
    private long chatId;
    private LocalDateTime getTime;  //время получения задания
    private LocalDateTime planTime;  //плановое время выполения задания, c точностью до минуты
    private LocalDateTime factTime;  //фактическое время выполения задания. Если null, то неисполнено
    private String message; //сообщение выводимое по заданию

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public LocalDateTime getGetTime() {
        return getTime;
    }

    public void setGetTime(LocalDateTime getTime) {
        this.getTime = getTime;
    }

    public LocalDateTime getPlanTime() {
        return planTime;
    }

    public void setPlanTime(LocalDateTime planTime) {
        this.planTime = planTime;
    }

    public LocalDateTime getFactTime() {
        return factTime;
    }

    public void setFactTime(LocalDateTime factTime) {
        this.factTime = factTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;

        Task task = (Task) o;

        if (id != task.id) return false;
        if (chatId != task.chatId) return false;
        if (!Objects.equals(getTime, task.getTime)) return false;
        if (!Objects.equals(planTime, task.planTime)) return false;
        if (!Objects.equals(factTime, task.factTime)) return false;
        return Objects.equals(message, task.message);
    }
}
