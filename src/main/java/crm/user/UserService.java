package crm.user;

import crm.common.exception.NotFoundException;
import crm.role.Role;
import crm.role.RoleRepository;
import crm.user.dto.UserCreateDto;
import crm.user.dto.UserResponseDto;
import crm.user.dto.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // crm/user/UserService.java
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new NotFoundException("Role not found"));

        String rawPassword = (dto.getPassword() != null && !dto.getPassword().isBlank())
                ? dto.getPassword()
                : generateTempPassword();

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setRole(role);
        user.setDepartment(dto.getDepartment()); // Сохраняем департамент
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        UserResponseDto response = UserResponseDto.from(userRepository.save(user));
        response.setTempPassword(rawPassword);
        return response;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponseDto.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream() // Открываем поток данных
                .map(UserResponseDto::from) // Превращаем каждого User из базы в красивый DTO
                .toList(); // Собираем всё обратно в список (удобная фишка Java 16+)
    }

    @Transactional
    public UserResponseDto editUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }

        return UserResponseDto.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false); // Снимаем флаг после успешной смены
        userRepository.save(user);
    }



    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}