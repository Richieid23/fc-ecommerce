package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.model.Role;
import id.web.fitrarizki.ecommerce.model.User;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.repository.RoleRepository;
import id.web.fitrarizki.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByKeyword(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Role> roles = roleRepository.findByUserId(user.getId());

        return UserInfo.builder()
                .user(user)
                .roles(roles)
                .build();
    }
}
