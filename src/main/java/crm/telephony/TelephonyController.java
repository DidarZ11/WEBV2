package crm.telephony;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import crm.common.response.ApiResponse;
import crm.telephony.dto.CallRequestDto;
import crm.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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

    @PostMapping(value = "/twiml/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleVoice(@RequestParam MultiValueMap<String, String> params) {
        log.info("=== TwiML FULL params: {}", params);

        String from = params.getFirst("From");

        // Twilio передаёт кастомные params с префиксом или напрямую
        // Пробуем все варианты
        String to = params.getFirst("To");
        if (to == null || to.isBlank()) to = params.getFirst("PhoneNumber");
        if (to == null || to.isBlank()) to = params.getFirst("Called");
        // Twilio иногда добавляет кастомные params без префикса
        for (String key : params.keySet()) {
            if (to == null || to.isBlank()) {
                String val = params.getFirst(key);
                if (val != null && val.startsWith("+") && val.length() > 7) {
                    log.info("Found phone in param key={}, val={}", key, val);
                    to = val;
                }
            }
        }

        log.info("from={}, to={}", from, to);

        if (from != null && from.startsWith("client:")) {
            if (to == null || to.isBlank()) {
                log.warn("Outgoing call but To is empty!");
                return new VoiceResponse.Builder()
                        .say(new Say.Builder("Номер не указан")
                                .language(Say.Language.RU_RU).build())
                        .build().toXml();
            }
            return twilioService.handleOutgoingCall(to);
        }

        // Входящий звонок
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
    public ResponseEntity<ApiResponse<List<CallRequestDto>>> getCallHistory() {
        List<CallRequestDto> history = callRequestRepository.findAll()
                .stream()
                .filter(c -> c.getStatus() == CallStatus.COMPLETED || c.getStatus() == CallStatus.IN_PROGRESS)
                .map(CallRequestDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}