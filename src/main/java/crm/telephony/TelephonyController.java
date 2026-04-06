package crm.telephony;

import crm.common.response.ApiResponse;
import crm.telephony.dto.CallRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telephony")
@RequiredArgsConstructor
public class TelephonyController {

    private final CallRequestRepository callRequestRepository;

    // 1. Симуляция: Twilio присылает нам звонок
    @PostMapping("/webhook/simulate")
    public ResponseEntity<ApiResponse<CallRequestDto>> simulateIncomingCall(@RequestParam String phone) {
        CallRequest call = new CallRequest();
        call.setClientPhone(phone);
        call.setStatus(CallStatus.NEW);

        CallRequest savedCall = callRequestRepository.save(call);

        // TODO: Здесь мы позже добавим WebSockets,
        // чтобы мгновенно пушить этот звонок во фронтенд!

        return ResponseEntity.ok(ApiResponse.ok(CallRequestDto.from(savedCall)));
    }

    // 2. Получить список всех новых звонков (чтобы фронтенд мог их показать)
    @GetMapping("/calls/active")
    public ResponseEntity<ApiResponse<List<CallRequestDto>>> getActiveCalls() {
        List<CallRequestDto> activeCalls = callRequestRepository.findByStatus(CallStatus.NEW)
                .stream()
                .map(CallRequestDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(activeCalls));
    }
}