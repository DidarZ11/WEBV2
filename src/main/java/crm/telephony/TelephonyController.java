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

    // 1. Токен для браузера — фронт вызывает при логине оператора
    @GetMapping("/token")
    public ResponseEntity<ApiResponse<String>> getToken(
            @AuthenticationPrincipal User currentUser) {
        // Берем email текущего юзера как identity для Twilio
        String token = twilioService.generateAccessToken(currentUser.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    // 2. Webhook — Twilio вызывает, когда поступает реальный звонок на номер
    @PostMapping(value = "/twiml/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleIncomingCall() {
        // Мы жестко направляем звонок на админа для теста.
        // Identity должен СОВПАДАТЬ с тем, что в токене на фронте.
        return twilioService.handleIncomingCall("admin@crm.kz");
    }

    // 3. Симуляция входящего звонка (только для отрисовки карточки через WebSocket)
    @PostMapping("/webhook/simulate")
    public ResponseEntity<ApiResponse<CallRequestDto>> simulateIncomingCall(
            @RequestParam String phone) {
        CallRequest call = new CallRequest();
        call.setClientPhone(phone);
        call.setStatus(CallStatus.NEW);

        CallRequest savedCall = callRequestRepository.save(call);
        CallRequestDto callDto = CallRequestDto.from(savedCall);

        // Отправляем уведомление по веб-сокетам
        messagingTemplate.convertAndSend("/topic/calls", callDto);

        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    // 4. Оператор нажимает "Ответить" — фиксируем в базе
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

        // Сообщаем остальным, что звонок взят
        messagingTemplate.convertAndSend("/topic/calls/answered", callDto);

        return ResponseEntity.ok(ApiResponse.ok(callDto));
    }

    // 5. Завершить звонок
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

    // 6. Получить список активных звонков (очередь)
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