package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.user.UserRegisterRequest;
import id.web.fitrarizki.ecommerce.dto.user.UserResponse;
import id.web.fitrarizki.ecommerce.dto.user.UserUpdateRequest;
import id.web.fitrarizki.ecommerce.exception.*;
import id.web.fitrarizki.ecommerce.model.Role;
import id.web.fitrarizki.ecommerce.model.User;
import id.web.fitrarizki.ecommerce.model.UserRole;
import id.web.fitrarizki.ecommerce.repository.RoleRepository;
import id.web.fitrarizki.ecommerce.repository.UserRepository;
import id.web.fitrarizki.ecommerce.repository.UserRoleRepository;
import id.web.fitrarizki.ecommerce.service.CacheService;
import id.web.fitrarizki.ecommerce.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    private final String USER_CACHE_KEY = "user:";
    private final String USER_ROLES_CACHE_KEY = "user:roles:";

    @Override
    @Transactional
    public UserResponse register(UserRegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistException("Username already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(encodedPassword)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RoleNotFoundException("Default Role not found"));

        UserRole userRole = UserRole.builder()
                .id(new UserRole.UserRoleId(user.getId(), role.getId()))
                .build();
        userRoleRepository.save(userRole);

        return UserResponse.fromUserAndRoles(user, Collections.singletonList(role));
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Role> roles = roleRepository.findByUserId(user.getId());

        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    public UserResponse getUserByKeyword(String keyword) {
        User user = userRepository.findByKeyword(keyword).orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Role> roles = roleRepository.findByUserId(user.getId());

        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userUpdateRequest.getCurrentPassword() != null && userUpdateRequest.getNewPassword() != null) {
            if (!passwordEncoder.matches(userUpdateRequest.getCurrentPassword(), user.getPassword())) {
                throw new InvalidPasswordException("Current password does not match");
            }

            String encodedPassword = passwordEncoder.encode(userUpdateRequest.getNewPassword());
            user.setPassword(encodedPassword);
        }

        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userUpdateRequest.getUsername())) {
                throw new UsernameAlreadyExistException("Username already taken");
            }

            user.setUsername(userUpdateRequest.getUsername());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userUpdateRequest.getEmail())) {
                throw new EmailAlreadyExistException("Email already exists");
            }

            user.setEmail(userUpdateRequest.getEmail());
        }

        userRepository.save(user);
        List<Role> roles = roleRepository.findByUserId(user.getId());

        String userCacheKey = USER_CACHE_KEY + user.getUsername();
        String userRolesCacheKey = USER_ROLES_CACHE_KEY + user.getUsername();
        cacheService.evict(userCacheKey);
        cacheService.evict(userRolesCacheKey);

        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        userRoleRepository.deleteByIdUserId(user.getId());
        userRepository.delete(user);
    }
}
