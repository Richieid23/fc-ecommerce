package id.web.fitrarizki.ecommerce.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import id.web.fitrarizki.ecommerce.model.Role;
import id.web.fitrarizki.ecommerce.model.User;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.repository.RoleRepository;
import id.web.fitrarizki.ecommerce.repository.UserRepository;
import id.web.fitrarizki.ecommerce.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CacheService cacheService;

    private final String USER_CACHE_KEY = "user:";
    private final String USER_ROLES_CACHE_KEY = "user:roles:";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userCacheKey = USER_CACHE_KEY + username;
        String userRolesCacheKey = USER_ROLES_CACHE_KEY + username;

        Optional<User> userOpt = cacheService.get(userCacheKey, User.class);
        Optional<List<Role>> rolesOpt = cacheService.get(userRolesCacheKey, new TypeReference<List<Role>>() {});

        if (userOpt.isPresent() && rolesOpt.isPresent()) {
            return UserInfo.builder()
                    .user(userOpt.get())
                    .roles(rolesOpt.get())
                    .build();
        }

        User user = userRepository.findByKeyword(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Role> roles = roleRepository.findByUserId(user.getId());

        UserInfo userInfo = UserInfo.builder()
                .user(user)
                .roles(roles)
                .build();

        cacheService.set(userCacheKey, user);
        cacheService.set(userRolesCacheKey, roles);

        return userInfo;
    }
}
