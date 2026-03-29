package crm.schedule;

import crm.common.response.ApiResponse;
import crm.schedule.dto.ScheduleCreateDto;
import crm.schedule.dto.ScheduleResponseDto;
import crm.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleEntryService scheduleEntryService;

    // POST /api/v1/schedules — создать черновик
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> create(
            @RequestBody ScheduleCreateDto dto,
            @AuthenticationPrincipal User currentUser) {

        var schedule = scheduleService.create(dto, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    // GET /api/v1/schedules/branch/{branchId} — все графики филиала
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<ScheduleResponseDto>>> getByBranch(
            @PathVariable Long branchId) {

        var list = scheduleService.getByBranch(branchId)
                .stream()
                .map(ScheduleResponseDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // GET /api/v1/schedules/{id} — получить один график
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> getById(
            @PathVariable Long id) {

        var schedule = scheduleService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    // PATCH /api/v1/schedules/{id}/submit — отправить на утверждение
    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        var schedule = scheduleService.submit(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    // PATCH /api/v1/schedules/{id}/approve — утвердить
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        var schedule = scheduleService.approve(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    // PATCH /api/v1/schedules/{id}/reject — отклонить
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')") // Или 'HR' / 'MANAGER' согласно твоим ролям
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> reject(
            @PathVariable Long id,
            @RequestParam("comment") String comment, // <-- ДОБАВИЛИ ПАРАМЕТР
            @AuthenticationPrincipal User currentUser) {

        var schedule = scheduleService.reject(id, currentUser.getId(), comment);
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    // PUT /api/v1/schedules/{id}/entries — заполнить таблицу графика смен
    @PutMapping("/{id}/entries")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> fillScheduleEntries(
            @PathVariable Long id,
            @RequestBody @Valid java.util.List<crm.schedule.dto.ScheduleEntryDto> entries) {

        scheduleEntryService.fillSchedule(id, entries);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}