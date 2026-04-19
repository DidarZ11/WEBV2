package crm.telephony;

import crm.common.response.ApiResponse;
import crm.telephony.dto.CallRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/api/v1/telephony")
@RequiredArgsConstructor
public class TelephonyController {

    private final CallRequestRepository callRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TwilioService twilioService;

    // Храним последний подготовленный номер — простое решение для одного оператора
    // Для нескольких операторов нужна будет полноценная БД запись
    private final AtomicReference<String> lastPreparedNumber = new AtomicReference<>("");

    @GetMapping("/token")
    public ResponseEntity<ApiResponse<String>> getToken() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String token = twilioService.generateAccessToken(email);
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @PostMapping("/outgoing/prepare")
    public ResponseEntity<ApiResponse<String>> prepareOutgoingCall(@RequestParam String phoneNumber) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        lastPreparedNumber.set(phoneNumber);
        log.info("Prepared outgoing call for {} to {}", email, phoneNumber);
        return ResponseEntity.ok(ApiResponse.ok("ok"));
    }

    @PostMapping(value = "/twiml/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleVoice(@RequestParam MultiValueMap<String, String> params) {
        log.info("=== TwiML params: {}", params);

        String from = params.getFirst("From");
        String direction = params.getFirst("Direction");

        log.info("from={}, direction={}", from, direction);

        // Исходящий звонок с браузера (from всегда начинается с "client:")
        if (from != null && from.startsWith("client:")) {
            String phoneNumber = lastPreparedNumber.getAndSet("");

            if (phoneNumber == null || phoneNumber.isBlank()) {
                log.warn("No prepared phone number! from={}", from);
                return "<Response><Say language=\"ru-RU\">Номер не указан.</Say></Response>";
            }

            log.info("Outgoing call from {} to {}", from, phoneNumber);
            return twilioService.handleOutgoingCall(phoneNumber);
        }

        // Входящий звонок с реального телефона
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
    public ResponseEntity<ApiResponse<CallRequestDto>> answerCall(@PathVariable Long id) {
        CallRequest call = callRequestRepository.findById(id).orElseThrow();
        call.setStatus(CallStatus.IN_PROGRESS);
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