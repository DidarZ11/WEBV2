package crm.telephony;

import crm.common.response.ApiResponse;
import crm.telephony.dto.CallRequestDto;
import crm.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telephony")
@RequiredArgsConstructor
public class TelephonyController {

    private final CallRequestRepository callRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TwilioService twilioService;

    // Токен для браузера — фронт вызывает при логине оператора
    @GetMapping("/token")
    public ResponseEntity<ApiResponse<String>> getToken(
            @AuthenticationPrincipal User currentUser) {
        String token = twilioService.generateAccessToken(currentUser.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    // Webhook — Twilio вызывает когда поступает реальный звонок
    @PostMapping(value = "/twiml/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleIncomingCall() {
        return twilioService.handleIncomingCall();
    }

    // Симуляция входящего звонка для тестирования
    @PostMapping("/webhook/simulate")
    public ResponseEntity<ApiResponse<CallRequestDto>> simulateIncomingCall(
            @RequestParam String phone) {
        CallRequest call = new CallRequest();
        call.setClientPhone(phone);
        call.setStatus(CallStatus.NEW);

        CallRequest savedCall = callRequestRepository.save(call);
        CallRequestDto callDto = CallRequestDto.from(savedCall);

        messagingTemplate.convertAndSend("/topic/calls", callDto);

        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    // Оператор берёт трубку — захватывает звонок
    @PostMapping("/calls/{id}/answer")
    public ResponseEntity<ApiResponse<CallRequestDto>> answerCall(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        CallRequest call = callRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        if (call.getStatus() != CallStatus.NEW) {
            throw new IllegalStateException("Call already taken");
        }

        call.setStatus(CallStatus.IN_PROGRESS);
        call.setOperator(currentUser);
        callRequestRepository.save(call);

        CallRequestDto callDto = CallRequestDto.from(call);

        // Уведомляем всех операторов что звонок взят
        messagingTemplate.convertAndSend("/topic/calls/answered", callDto);

        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    // Завершить звонок
    @PostMapping("/calls/{id}/complete")
    public ResponseEntity<ApiResponse<CallRequestDto>> completeCall(
            @PathVariable Long id) {
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
        List<CallRequestDto> activeCalls = callRequestRepository
                .findByStatus(CallStatus.NEW)
                .stream()
                .map(CallRequestDto::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(activeCalls));
    }
}