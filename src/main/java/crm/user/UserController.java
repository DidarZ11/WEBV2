package crm.user;

import crm.common.response.ApiResponse; // Исправлен путь до твоего ApiResponse
import crm.user.dto.UserCreateDto;
import crm.user.dto.UserResponseDto;
import crm.user.dto.UserUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('ADMIN')")
    public ApiResponse<UserResponseDto> add(@RequestBody @Valid UserCreateDto dto) {
        return ApiResponse.ok(userService.createUser(dto));
    }

    @GetMapping("/info")
    public ApiResponse<UserResponseDto> info(Authentication authentication) {
        return ApiResponse.ok(userService.getUserInfo(authentication.getName()));
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT') or hasRole('ADMIN')")
    public ApiResponse<UserResponseDto> edit(@PathVariable Long id, @RequestBody @Valid UserUpdateDto dto) {
        return ApiResponse.ok(userService.editUser(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.ok(null);
    }
}