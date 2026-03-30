package crm.schedule.dto;

import crm.schedule.Schedule;
import crm.schedule.ScheduleStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class ScheduleResponseDto {

    private Long id;
    private Long branchId;
    private String branchName;
    private Integer month;
    private Integer year;
    private ScheduleStatus status;
    private Integer version;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private List<EntryDto> entries;

    @Data
    public static class EntryDto {
        private Long id;
        private Long userId;
        private String userFullName;
        private LocalDate date;
        private String shiftType;
        private LocalTime shiftStart;
        private LocalTime shiftEnd;
        private String notes;
    }

    public static ScheduleResponseDto from(Schedule s) {
        var dto = new ScheduleResponseDto();
        dto.setId(s.getId());
        dto.setBranchId(s.getBranch().getId());
        dto.setBranchName(s.getBranch().getName());
        dto.setMonth(s.getMonth());
        dto.setYear(s.getYear());
        dto.setStatus(s.getStatus());
        dto.setVersion(s.getVersion());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setApprovedAt(s.getApprovedAt());

        if (s.getCreatedBy() != null)
            dto.setCreatedBy(s.getCreatedBy().getEmail());
        if (s.getApprovedBy() != null)
            dto.setApprovedBy(s.getApprovedBy().getEmail());

        if (s.getEntries() != null) {
            dto.setEntries(s.getEntries().stream().map(e -> {
                EntryDto entry = new EntryDto();
                entry.setId(e.getId());
                entry.setUserId(e.getUser().getId());
                entry.setUserFullName(e.getUser().getFullName());
                entry.setDate(e.getDate());
                entry.setShiftType(e.getShiftType().name());
                entry.setShiftStart(e.getShiftStart());
                entry.setShiftEnd(e.getShiftEnd());
                entry.setNotes(e.getNotes());
                return entry;
            }).toList());
        }

        return dto;
    }
}