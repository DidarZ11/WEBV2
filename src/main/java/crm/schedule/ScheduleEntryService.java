package crm.schedule;

import crm.common.exception.ForbiddenException;
import crm.common.exception.NotFoundException;
import crm.schedule.dto.ScheduleEntryDto;
import crm.user.User;
import crm.user.UserRepository; // ДОБАВИЛИ ИМПОРТ
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleEntryService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository entryRepository;
    private final UserRepository userRepository; // ДОБАВИЛИ РЕПОЗИТОРИЙ ПОЛЬЗОВАТЕЛЕЙ

    @Transactional
    public void fillSchedule(Long scheduleId, List<ScheduleEntryDto> entriesDto) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("График не найден"));

        // БИЗНЕС-ЛОГИКА: Запрет на редактирование
        if (schedule.getStatus() == ScheduleStatus.PENDING ||
                schedule.getStatus() == ScheduleStatus.APPROVED) {
            throw new ForbiddenException("Редактирование графика запрещено в статусах 'Ожидает одобрения' и 'Утвержден'");
        }

        entryRepository.deleteAllByScheduleId(scheduleId);

        List<ScheduleEntry> entries = entriesDto.stream().map(dto -> {
            ScheduleEntry entry = new ScheduleEntry();
            entry.setSchedule(schedule);

            // Используем getReferenceById для создания легковесной ссылки на User без лишнего запроса к БД
            User userRef = userRepository.getReferenceById(dto.getUserId());
            entry.setUser(userRef); // ИСПОЛЬЗУЕМ setUser вместо setUserId

            entry.setDate(dto.getDate());
            entry.setShiftType(dto.getShiftType());

            // Раскомментировали твои дополнительные поля
            entry.setShiftStart(dto.getShiftStart());
            entry.setShiftEnd(dto.getShiftEnd());
            entry.setNotes(dto.getNotes());

            return entry;
        }).collect(Collectors.toList());

        entryRepository.saveAll(entries);
    }
}