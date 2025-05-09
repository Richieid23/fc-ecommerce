package id.web.fitrarizki.ecommerce.dto.cart;

import id.web.fitrarizki.ecommerce.model.CartItem;
import id.web.fitrarizki.ecommerce.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal weight;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponse fromCartItemAndProduct(CartItem cartItem, Product product) {
        BigDecimal totalPrice = cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
        BigDecimal weight = product.getWeight().multiply(new BigDecimal(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .productName(product.getName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .weight(weight)
                .totalPrice(totalPrice)
                .build();
    }
}
