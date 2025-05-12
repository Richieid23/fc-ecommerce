package id.web.fitrarizki.ecommerce.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckOutRequest {
    private Long userId;

    @NotNull(message = "Cart items cannot be null")
    @Size(min = 1, message = "At least one cart item must be selected for checkout")
    private List<Long> cartItemIds;

    @NotNull(message = "User address cannot be null")
    private Long userAddressId;
}
