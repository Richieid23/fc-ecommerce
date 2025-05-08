package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.user.AuthRequest;
import id.web.fitrarizki.ecommerce.exception.InvalidCredentialsException;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;

    @Override
    public UserInfo authenticate(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            return (UserInfo) authentication.getPrincipal();
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid username or password)");
        }
    }
}
