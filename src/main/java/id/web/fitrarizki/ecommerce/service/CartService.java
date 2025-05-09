package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.cart.CartItemResponse;

import java.util.List;

public interface CartService {
    void addCartItem(Long userId, Long productId, int quantity);
    void updateCartItemQuantity(Long userId, Long productId, int quantity);
    void deleteCartItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
    List<CartItemResponse> getCartItems(Long userId);
}
