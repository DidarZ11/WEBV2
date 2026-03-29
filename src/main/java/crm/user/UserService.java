package crm.user;

import crm.common.exception.NotFoundException; // Исправлен путь до твоего исключения
import crm.role.Role;
import crm.role.RoleRepository;
import crm.user.dto.UserCreateDto;
import crm.user.dto.UserResponseDto;
import crm.user.dto.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // ДОБАВИЛИ РЕПОЗИТОРИЙ
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        // НАХОДИМ РОЛЬ В БАЗЕ ПО ID
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new NotFoundException("Role not found"));

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());

        user.setRole(role); // ПРИВЯЗЫВАЕМ РОЛЬ К ПОЛЬЗОВАТЕЛЮ

        return UserResponseDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto editUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName()); // Заменяем firstName и lastName
        }

        // Дополнительные поля для обновления, если появятся

        return UserResponseDto.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}