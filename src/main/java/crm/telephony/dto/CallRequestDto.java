package crm.telephony.dto;

import crm.telephony.CallRequest;
import crm.telephony.CallStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CallRequestDto {
    private Long id;
    private String clientPhone;
    private CallStatus status;
    private LocalDateTime createdAt;

    public static CallRequestDto from(CallRequest call) {
        return CallRequestDto.builder()
                .id(call.getId())
                .clientPhone(call.getClientPhone())
                .status(call.getStatus())
                .createdAt(call.getCreatedAt())
                .build();
    }
}