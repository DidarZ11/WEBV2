package crm.schedule.dto;

import lombok.Data;

@Data
public class ScheduleCreateDto {
    private Long branchId;
    private Integer month;  // 1-12
    private Integer year;
}