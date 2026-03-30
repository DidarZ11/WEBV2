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

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'SCHEDULE_CREATE')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> create(
            @RequestBody ScheduleCreateDto dto,
            @AuthenticationPrincipal User currentUser) {
        var schedule = scheduleService.create(dto, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'EMPLOYEE', 'SCHEDULE_VIEW')")
    public ResponseEntity<ApiResponse<List<ScheduleResponseDto>>> getByBranch(
            @PathVariable Long branchId) {
        var list = scheduleService.getByBranch(branchId)
                .stream()
                .map(ScheduleResponseDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'EMPLOYEE', 'SCHEDULE_VIEW')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> getById(
            @PathVariable Long id) {
        var schedule = scheduleService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'SCHEDULE_SUBMIT')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        var schedule = scheduleService.submit(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SCHEDULE_APPROVE')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        var schedule = scheduleService.approve(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SCHEDULE_APPROVE')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> reject(
            @PathVariable Long id,
            @RequestParam("comment") String comment,
            @AuthenticationPrincipal User currentUser) {
        var schedule = scheduleService.reject(id, currentUser.getId(), comment);
        return ResponseEntity.ok(ApiResponse.ok(ScheduleResponseDto.from(schedule)));
    }

    @PutMapping("/{id}/entries")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'SCHEDULE_EDIT')")
    public ResponseEntity<ApiResponse<Void>> fillScheduleEntries(
            @PathVariable Long id,
            @RequestBody @Valid List<crm.schedule.dto.ScheduleEntryDto> entries) {
        scheduleEntryService.fillSchedule(id, entries);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}