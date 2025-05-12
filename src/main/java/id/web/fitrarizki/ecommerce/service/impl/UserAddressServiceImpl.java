package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.user.address.UserAddressRequest;
import id.web.fitrarizki.ecommerce.dto.user.address.UserAddressResponse;
import id.web.fitrarizki.ecommerce.exception.ForbiddenAccessException;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.UserAddress;
import id.web.fitrarizki.ecommerce.repository.UserAddressRepository;
import id.web.fitrarizki.ecommerce.service.UserAddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressServiceImpl implements UserAddressService {
    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional
    public UserAddressResponse createUserAddress(Long userId, UserAddressRequest userAddressRequest) {
        UserAddress newAddress = UserAddress.builder()
                .userId(userId)
                .addressName(userAddressRequest.getAddressName())
                .streetAddress(userAddressRequest.getStreetAddress())
                .city(userAddressRequest.getCity())
                .state(userAddressRequest.getState())
                .postalCode(userAddressRequest.getPostalCode())
                .country(userAddressRequest.getCountry())
                .isDefault(userAddressRequest.isDefault())
                .build();

        if (userAddressRequest.isDefault()) {
            Optional<UserAddress> defaultAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(userId);
            defaultAddress.ifPresent(userAddress -> {
                userAddress.setDefault(false);
                userAddressRepository.save(userAddress);
            });
        }

        return UserAddressResponse.fromUserAddress(userAddressRepository.save(newAddress));
    }

    @Override
    public List<UserAddressResponse> getUserAddressesByUserId(Long userId) {
        return userAddressRepository.findByUserId(userId).stream().map(UserAddressResponse::fromUserAddress).toList();
    }

    @Override
    public UserAddressResponse getUserAddressById(Long userAddressId) {
        return UserAddressResponse.fromUserAddress(userAddressRepository.findById(userAddressId).orElseThrow(() -> new ResourceNotFoundException("User address not found")));
    }

    @Override
    @Transactional
    public UserAddressResponse updateUserAddress(Long userAddressId, UserAddressRequest userAddressRequest) {
        UserAddress userAddress = userAddressRepository.findById(userAddressId).orElseThrow(() -> new ResourceNotFoundException("User address not found"));
        userAddress.setAddressName(userAddressRequest.getAddressName());
        userAddress.setStreetAddress(userAddressRequest.getStreetAddress());
        userAddress.setCity(userAddressRequest.getCity());
        userAddress.setState(userAddressRequest.getState());
        userAddress.setPostalCode(userAddressRequest.getPostalCode());
        userAddress.setCountry(userAddressRequest.getCountry());

        if (userAddressRequest.isDefault() && !userAddress.isDefault()) {
            Optional<UserAddress> defaultAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(userAddress.getUserId());
            defaultAddress.ifPresent(address -> {
                address.setDefault(false);
                userAddressRepository.save(address);
            });
        }

        userAddress.setDefault(userAddressRequest.isDefault());

        return UserAddressResponse.fromUserAddress(userAddressRepository.save(userAddress));
    }

    @Override
    public void deleteUserAddressById(Long userAddressId) {
        UserAddress userAddress = userAddressRepository.findById(userAddressId).orElseThrow(() -> new ResourceNotFoundException("User address not found"));
        userAddressRepository.delete(userAddress);

        if (userAddress.isDefault()) {
            List<UserAddress> addresses = userAddressRepository.findByUserId(userAddress.getUserId());
            if (!addresses.isEmpty()) {
                UserAddress newDefaultAddress = addresses.getFirst();
                newDefaultAddress.setDefault(true);
                userAddressRepository.save(newDefaultAddress);
            }
        }
    }

    @Override
    public UserAddressResponse setDefaultUserAddress(Long userId, Long userAddressId) {
        UserAddress userAddress = userAddressRepository.findById(userAddressId).orElseThrow(() -> new ResourceNotFoundException("User address not found"));

        if (!userAddress.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("Cannot update user address of another user");
        }

        Optional<UserAddress> defaultAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(userId);
        defaultAddress.ifPresent(address -> {
            address.setDefault(false);
            userAddressRepository.save(address);
        });

        userAddress.setDefault(true);
        return UserAddressResponse.fromUserAddress(userAddressRepository.save(userAddress)) ;
    }
}
