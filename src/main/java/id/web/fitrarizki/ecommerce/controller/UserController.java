package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.dto.user.UserResponse;
import id.web.fitrarizki.ecommerce.dto.user.UserUpdateRequest;
import id.web.fitrarizki.ecommerce.exception.ForbiddenAccessException;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserResponse userResponse = UserResponse.fromUserAndRoles(userInfo.getUser(), userInfo.getRoles());
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        if (!userInfo.getUser().getId().equals(id) && !userInfo.getAuthorities().contains("ROLE_ADMIN")) {
            throw new ForbiddenAccessException(
                    "user " + userInfo.getUsername() + " not allowed to update");
        }
        UserResponse updatedUser = userService.updateUser(id, userUpdateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        if (!userInfo.getUser().getId().equals(id) && !userInfo.getAuthorities().contains("ROLE_ADMIN")) {
            throw new ForbiddenAccessException(
                    "user " + userInfo.getUsername() + " not allowed to delete");
        }

        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
