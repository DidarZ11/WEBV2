package crm.schedule;

import crm.branch.BranchRepository;
import crm.common.exception.ForbiddenException;
import crm.common.exception.NotFoundException;
import crm.schedule.dto.ScheduleCreateDto;
import crm.schedule.dto.ScheduleResponseDto;
import crm.user.User;
import crm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ScheduleVersionRepository versionRepository; // ДОБАВИЛИ РЕПОЗИТОРИЙ ВЕРСИЙ

    // ─── Создать черновик графика ────────────────────────────────────────────
    public Schedule create(ScheduleCreateDto dto, Long creatorId) {
        var branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        var creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean exists = scheduleRepository
                .existsByBranchIdAndMonthAndYear(dto.getBranchId(), dto.getMonth(), dto.getYear());
        if (exists) {
            throw new IllegalStateException("Schedule for this branch/month/year already exists");
        }

        var schedule = Schedule.builder()
                .branch(branch)
                .month(dto.getMonth())
                .year(dto.getYear())
                .status(ScheduleStatus.DRAFT) // Сначала статус "Черновик"
                .version(1)
                .createdBy(creator)
                .build();

        return scheduleRepository.save(schedule);
    }

    // ─── Отправить на утверждение ────────────────────────────────────────────
    public Schedule submit(Long scheduleId, Long userId) {
        var schedule = getScheduleOrThrow(scheduleId);

        if (schedule.getStatus() != ScheduleStatus.DRAFT
                && schedule.getStatus() != ScheduleStatus.REJECTED) {
            throw new IllegalStateException("Only DRAFT or REJECTED schedule can be submitted");
        }

        schedule.setStatus(ScheduleStatus.PENDING); // Ожидает одобрения
        schedule.setVersion(schedule.getVersion() + 1); // Повышаем версию при повторной отправке

        // Логируем это действие в историю
        saveVersionHistory(schedule, userId, "Отправлен на согласование");

        return scheduleRepository.save(schedule);
    }

    // ─── Утвердить график ────────────────────────────────────────────────────
    public Schedule approve(Long scheduleId, Long approverId) {
        var schedule = getScheduleOrThrow(scheduleId);

        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new IllegalStateException("Only PENDING schedule can be approved");
        }

        var approver = userRepository.findById(approverId)
                .orElseThrow(() -> new NotFoundException("Approver not found"));

        schedule.setStatus(ScheduleStatus.APPROVED);
        schedule.setApprovedBy(approver);
        schedule.setApprovedAt(LocalDateTime.now());

        saveVersionHistory(schedule, approverId, "Утвержден");

        return scheduleRepository.save(schedule);
    }

    // ─── Отклонить график (Возврат на доработку) ──────────────────────────────
    public Schedule reject(Long scheduleId, Long approverId, String comment) {
        var schedule = getScheduleOrThrow(scheduleId);

        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new IllegalStateException("Only PENDING schedule can be rejected");
        }

        // БИЗНЕС-ЛОГИКА: Комментарий обязателен
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Комментарий обязателен при возврате на доработку");
        }

        schedule.setStatus(ScheduleStatus.REJECTED); // На доработке

        // Сохраняем комментарий HR в историю версий
        saveVersionHistory(schedule, approverId, comment);

        return scheduleRepository.save(schedule);
    }

    // ─── Получить все графики филиала ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Schedule> getByBranch(Long branchId) {
        return scheduleRepository.findAllByBranchId(branchId);
    }

    // ─── Получить один график ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Schedule getById(Long scheduleId) {
        return getScheduleOrThrow(scheduleId);
    }

    // ─── Приватные хелперы ───────────────────────────────────────────────────
    private Schedule getScheduleOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + id));
    }

    // Вспомогательный метод для сохранения истории версий
    private void saveVersionHistory(Schedule schedule, Long userId, String comment) {
        var user = userRepository.getReferenceById(userId);

        // Предполагается, что в ScheduleVersion у тебя есть связи @ManyToOne к Schedule и User
        ScheduleVersion version = new ScheduleVersion();
        version.setSchedule(schedule);
        version.setVersionNumber(schedule.getVersion());
        version.setChangedBy(user);
        version.setChangedAt(LocalDateTime.now());
        version.setComment(comment);

        versionRepository.save(version);
    }

    public Schedule reopen(Long scheduleId, Long userId) {
        var schedule = getScheduleOrThrow(scheduleId);

        if (schedule.getStatus() != ScheduleStatus.REJECTED && schedule.getStatus() != ScheduleStatus.APPROVED) {
            throw new IllegalStateException("Only REJECTED or APPROVED schedules can be reopened");
        }

        schedule.setStatus(ScheduleStatus.DRAFT);
        schedule.setVersion(schedule.getVersion() + 1);

        saveVersionHistory(schedule, userId, "Возвращён в черновик для редактирования");

        return scheduleRepository.save(schedule);
    }
}