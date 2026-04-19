package crm.telephony;

import crm.common.response.ApiResponse;
import crm.telephony.dto.CallRequestDto;
import crm.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<ApiResponse<String>> getToken() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        String token = twilioService.generateAccessToken(email);
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    /**
     * GET /twiml/voice — для проверки доступности эндпоинта из браузера
     */
    @GetMapping(value = "/twiml/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleVoiceGet() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Say>OK</Say></Response>";
    }

    /**
     * POST /twiml/voice — основной webhook для Twilio
     * Twilio всегда шлёт POST с параметрами From и To
     */
    @PostMapping(value = "/twiml/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleVoice(@RequestParam(required = false) MultiValueMap<String, String> params) {
        String from = params != null ? params.getFirst("From") : null;
        String to   = params != null ? params.getFirst("To")   : null;

        // ИСХОДЯЩИЙ: звонок идёт из браузера (From = "client:email@domain.com")
        if (from != null && from.startsWith("client:")) {
            if (to == null || to.isBlank()) {
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Response><Say language=\"ru-RU\">Номер не указан.</Say></Response>";
            }
            return twilioService.handleOutgoingCall(to);
        }

        // ВХОДЯЩИЙ: звонок с реального телефона
        CallRequest call = new CallRequest();
        call.setClientPhone(from != null ? from : "Unknown");
        call.setStatus(CallStatus.NEW);
        CallRequest savedCall = callRequestRepository.save(call);

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
        if (call.getStatus() != CallStatus.NEW) {
            throw new IllegalStateException("Call already taken");
        }
        call.setStatus(CallStatus.IN_PROGRESS);
        call.setOperator(currentUser);
        callRequestRepository.save(call);
        CallRequestDto callDto = CallRequestDto.from(call);
        messagingTemplate.convertAndSend("/topic/calls/answered", callDto);
        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    @PostMapping("/calls/{id}/complete")
    public ResponseEntity<ApiResponse<CallRequestDto>> completeCall(@PathVariable Long id) {
        CallRequest call = callRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Call not found"));
        call.setStatus(CallStatus.COMPLETED);
        callRequestRepository.save(call);
        CallRequestDto callDto = CallRequestDto.from(call);
        messagingTemplate.convertAndSend("/topic/calls/completed", callDto);
        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    @GetMapping("/calls/active")
    public ResponseEntity<ApiResponse<List<CallRequestDto>>> getActiveCalls() {
        List<CallRequestDto> activeCalls = callRequestRepository.findByStatus(CallStatus.NEW)
                .stream().map(CallRequestDto::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(activeCalls));
    }

    @GetMapping("/calls/history")
    public ResponseEntity<ApiResponse<List<CallRequestDto>>> getCallsHistory() {
        List<CallRequestDto> history = callRequestRepository.findAll()
                .stream().map(CallRequestDto::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}