package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.user.address.UserAddressRequest;
import id.web.fitrarizki.ecommerce.dto.user.address.UserAddressResponse;

import java.util.List;

public interface UserAddressService {
    UserAddressResponse createUserAddress(Long userId, UserAddressRequest userAddressRequest);
    List<UserAddressResponse> getUserAddressesByUserId(Long userId);
    UserAddressResponse getUserAddressById(Long userAddressId);
    UserAddressResponse updateUserAddress(Long userAddressId, UserAddressRequest userAddressRequest);
    void deleteUserAddressById(Long userAddressId);
    UserAddressResponse setDefaultUserAddress(Long userId, Long userAddressId);
}
