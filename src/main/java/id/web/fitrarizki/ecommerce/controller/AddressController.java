package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.dto.user.address.UserAddressRequest;
import id.web.fitrarizki.ecommerce.dto.user.address.UserAddressResponse;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.UserAddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class AddressController {

    private final UserAddressService userAddressService;

    @PostMapping
    public ResponseEntity<UserAddressResponse> postAddress(@Valid @RequestBody UserAddressRequest userAddressRequest) {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(userAddressService.createUserAddress(userInfo.getUser().getId(), userAddressRequest));
    }

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getUserAddresses() {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userAddressService.getUserAddressesByUserId(userInfo.getUser().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAddressResponse> getUserAddress(@PathVariable Long id) {
        return ResponseEntity.ok(userAddressService.getUserAddressById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAddressResponse> updateUserAddress(@PathVariable Long id, @Valid @RequestBody UserAddressRequest userAddressRequest) {
        return ResponseEntity.ok(userAddressService.updateUserAddress(id, userAddressRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserAddress(@PathVariable Long id) {
        userAddressService.deleteUserAddressById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<UserAddressResponse> setDefaultAddress(@PathVariable Long id) {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userAddressService.setDefaultUserAddress(userInfo.getUser().getId(), id));
    }
}
