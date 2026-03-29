package crm.auth;

import crm.auth.dto.AuthResponse;
import crm.auth.dto.LoginRequest;
import crm.common.exception.NotFoundException;
import crm.user.User;
import crm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse register(LoginRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }
        var user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // ← было password
                .fullName(request.getFullName() != null ? request.getFullName() : request.getEmail())// временно, пока нет поля в запросе
                .isActive(true)
                .build();
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user));
    }
}