package crm.telephony;

import crm.common.response.ApiResponse;
import crm.telephony.dto.CallRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- НОВЫЙ ИМПОРТ
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telephony")
@RequiredArgsConstructor
public class TelephonyController {

    private final CallRequestRepository callRequestRepository;
    private final SimpMessagingTemplate messagingTemplate; // <-- ИНЖЕКТИМ ШАБЛОН

    @PostMapping("/webhook/simulate")
    public ResponseEntity<ApiResponse<CallRequestDto>> simulateIncomingCall(@RequestParam String phone) {
        CallRequest call = new CallRequest();
        call.setClientPhone(phone);
        call.setStatus(CallStatus.NEW);

        CallRequest savedCall = callRequestRepository.save(call);
        CallRequestDto callDto = CallRequestDto.from(savedCall);

        // ОТПРАВЛЯЕМ ЗВОНОК НА ФРОНТЕНД В РЕАЛЬНОМ ВРЕМЕНИ
        // Все React-клиенты, подписанные на "/topic/calls", мгновенно получат этот объект
        messagingTemplate.convertAndSend("/topic/calls", callDto);

        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    @GetMapping("/calls/active")
    public ResponseEntity<ApiResponse<List<CallRequestDto>>> getActiveCalls() {
        List<CallRequestDto> activeCalls = callRequestRepository.findByStatus(CallStatus.NEW)
                .stream()
                .map(CallRequestDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(activeCalls));
    }
}