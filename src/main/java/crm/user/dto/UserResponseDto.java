package crm.user.dto;

import crm.user.User;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String roleName;
    private String tempPassword; // показывается один раз при создании

    public static UserResponseDto from(User user) {
        if (user == null) return null;
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoleName(user.getRole().getName());
        return dto;
    }
}