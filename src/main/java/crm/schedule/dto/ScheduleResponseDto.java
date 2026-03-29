package crm.schedule.dto;

import crm.schedule.Schedule;
import crm.schedule.ScheduleStatus;
import lombok.Data;
import java.time.LocalDateTime;

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
        return dto;
    }
}