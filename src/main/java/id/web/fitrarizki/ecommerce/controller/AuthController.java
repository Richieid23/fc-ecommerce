package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.dto.user.AuthRequest;
import id.web.fitrarizki.ecommerce.dto.user.AuthResponse;
import id.web.fitrarizki.ecommerce.dto.user.UserRegisterRequest;
import id.web.fitrarizki.ecommerce.dto.user.UserResponse;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.AuthService;
import id.web.fitrarizki.ecommerce.service.JwtService;
import id.web.fitrarizki.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        UserInfo userInfo = authService.authenticate(authRequest);
        String token = jwtService.generateToken(userInfo);

        return ResponseEntity.ok(AuthResponse.fromUserInfoAndToken(userInfo, token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserResponse userResponse = userService.register(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
