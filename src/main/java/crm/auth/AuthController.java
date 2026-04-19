package crm.auth;

import crm.common.response.ApiResponse;
import crm.auth.dto.LoginRequest;
import crm.auth.dto.AuthResponse;
import crm.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor // Проверь, что эта аннотация есть сверху класса
public class AuthController {

    private final AuthService authService;
    private final UserService userService; // Добавь эту строку

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.register(request)));
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, Principal principal) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Пароль слишком короткий");
        }

        // Вызываем сервис для смены пароля
        userService.changePassword(principal.getName(), newPassword);

        return ResponseEntity.ok().body(Map.of("message", "Пароль успешно изменен"));
    }
}