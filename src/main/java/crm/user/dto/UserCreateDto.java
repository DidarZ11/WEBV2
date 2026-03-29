package crm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Не забудь этот импорт
import lombok.Data;

@Data
public class UserCreateDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    // ДОБАВИЛИ: ID роли, которую мы хотим назначить пользователю
    @NotNull(message = "ID роли обязателен")
    private Long roleId;
}