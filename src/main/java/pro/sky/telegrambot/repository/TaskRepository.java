package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.Task;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    //Параметр обрезать не будем, как написано в подсказке
    //LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).
    //Просто поищем LessThan, чтобы выполнить и пропущенные задачи тоже
    List<Task> findByPlanTimeLessThanAndFactTimeIsNull(LocalDateTime now);
}
