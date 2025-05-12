package id.web.fitrarizki.ecommerce.dto.user.address;

import id.web.fitrarizki.ecommerce.model.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResponse {
    private Long id;
    private String addressName;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean defaultAddress;

    public static UserAddressResponse fromUserAddress(UserAddress userAddress) {
        return UserAddressResponse.builder()
                .id(userAddress.getId())
                .addressName(userAddress.getAddressName())
                .streetAddress(userAddress.getStreetAddress())
                .city(userAddress.getCity())
                .state(userAddress.getState())
                .postalCode(userAddress.getPostalCode())
                .country(userAddress.getCountry())
                .defaultAddress(userAddress.isDefault())
                .build();
    }
}
