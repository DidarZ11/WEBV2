package crm.user.dto;

import crm.user.User;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName; // Изменили на fullName

    public static UserResponseDto from(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName()); // Берем из User
        return dto;
    }
}