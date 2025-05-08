package id.web.fitrarizki.ecommerce.dto.user;

import id.web.fitrarizki.ecommerce.model.Role;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private List<String> roles;

    public static AuthResponse fromUserInfoAndToken(UserInfo userInfo, String token) {
        return AuthResponse.builder()
                .token(token)
                .username(userInfo.getUsername())
                .email(userInfo.getUser().getEmail())
                .roles(userInfo.getRoles().stream().map(Role::getName).toList())
                .build();
    }
}
