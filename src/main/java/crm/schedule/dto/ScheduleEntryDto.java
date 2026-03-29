package crm.schedule.dto;

import crm.schedule.ShiftType;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleEntryDto {
    private Long userId;
    private LocalDate date;
    private ShiftType shiftType;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private String notes;
}