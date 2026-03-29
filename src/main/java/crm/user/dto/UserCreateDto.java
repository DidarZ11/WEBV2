package crm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateDto {
    @NotBlank
    @Email
    private String email;

    private String password; // необязательный — если пусто, генерируется автоматически

    @NotBlank
    private String fullName;

    @NotNull(message = "ID роли обязателен")
    private Long roleId;
}