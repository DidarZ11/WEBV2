package crm.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
    // Этот метод нам нужен для массовой перезаписи графика (удаляем старые смены, пишем новые)
    void deleteAllByScheduleId(Long scheduleId);
}