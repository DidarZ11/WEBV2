package crm.schedule.dto;

import crm.schedule.dto.ScheduleEntryDto;
import lombok.Data;
import java.util.List;

@Data
public class ScheduleUpdateDto {
    private List<ScheduleEntryDto> entries;
}