package crm.user.dto;

import crm.user.Department;
import crm.user.User;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String roleName;
    private Department department;
    private String tempPassword;
    private boolean mustChangePassword; // Передаем флаг на фронт

    public static UserResponseDto from(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleName(user.getRole().getName());
        dto.setDepartment(user.getDepartment());
        // Обязательно устанавливаем значение флага
        dto.setMustChangePassword(user.isMustChangePassword());
        return dto;
    }
}