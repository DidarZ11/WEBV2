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
    private String tempPassword; // показывается один раз при создании

    public static UserResponseDto from(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleName(user.getRole().getName());
        dto.setDepartment(user.getDepartment());
        return dto;
    }
}