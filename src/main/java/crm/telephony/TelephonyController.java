package crm.telephony;

import crm.common.response.ApiResponse;
import crm.telephony.dto.CallRequestDto;
import crm.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telephony")
@RequiredArgsConstructor
public class TelephonyController {

    private final CallRequestRepository callRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TwilioService twilioService;

    @GetMapping("/token")
    public ResponseEntity<ApiResponse<String>> getToken(@AuthenticationPrincipal User currentUser) {
        String token = twilioService.generateAccessToken(currentUser.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @PostMapping(value = "/twiml/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleVoice(@RequestParam MultiValueMap<String, String> params) {
        String from = params.getFirst("From");
        String to = params.getFirst("To");

        // 1. Если это исходящий с сайта (To не равен ID оператора)
        if (to != null && !to.isEmpty() && !to.equals("admin@crm.kz")) {
            return twilioService.handleOutgoingCall(to);
        }

        // 2. Иначе это входящий: Создаем карточку в базе
        CallRequest call = new CallRequest();
        call.setClientPhone(from != null ? from : "Unknown");
        call.setStatus(CallStatus.NEW);
        CallRequest savedCall = callRequestRepository.save(call);

        // Уведомляем фронтенд по WebSocket
        messagingTemplate.convertAndSend("/topic/calls", CallRequestDto.from(savedCall));

        return twilioService.handleIncomingCall("admin@crm.kz");
    }

    @PostMapping("/webhook/simulate")
    public ResponseEntity<ApiResponse<CallRequestDto>> simulateIncomingCall(@RequestParam String phone) {
        CallRequest call = new CallRequest();
        call.setClientPhone(phone);
        call.setStatus(CallStatus.NEW);
        CallRequest savedCall = callRequestRepository.save(call);
        CallRequestDto callDto = CallRequestDto.from(savedCall);
        messagingTemplate.convertAndSend("/topic/calls", callDto);
        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    @PostMapping("/calls/{id}/answer")
    public ResponseEntity<ApiResponse<CallRequestDto>> answerCall(
            @PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        CallRequest call = callRequestRepository.findById(id).orElseThrow();
        call.setStatus(CallStatus.IN_PROGRESS);
        call.setOperator(currentUser);
        callRequestRepository.save(call);

        CallRequestDto callDto = CallRequestDto.from(call);
        messagingTemplate.convertAndSend("/topic/calls/answered", callDto);
        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    @GetMapping("/calls/active")
    public ResponseEntity<ApiResponse<List<CallRequestDto>>> getActiveCalls() {
        List<CallRequestDto> activeCalls = callRequestRepository.findByStatus(CallStatus.NEW)
                .stream().map(CallRequestDto::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(activeCalls));
    }
}