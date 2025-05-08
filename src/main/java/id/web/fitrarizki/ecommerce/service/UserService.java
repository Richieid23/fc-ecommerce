package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.user.UserRegisterRequest;
import id.web.fitrarizki.ecommerce.dto.user.UserResponse;
import id.web.fitrarizki.ecommerce.dto.user.UserUpdateRequest;

public interface UserService {
    UserResponse register(UserRegisterRequest registerRequest);
    UserResponse getUserById(Long id);
    UserResponse getUserByKeyword(String keyword);
    UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest);
    void deleteUserById(Long id);
}
