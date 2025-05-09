package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.dto.cart.AddCartItemRequest;
import id.web.fitrarizki.ecommerce.dto.cart.CartItemResponse;
import id.web.fitrarizki.ecommerce.dto.cart.UpdateCartItemRequest;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<Void> addCartItem(@Valid @RequestBody AddCartItemRequest addCartItemRequest) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cartService.addCartItem(user.getUser().getId(), addCartItemRequest.getProductId(), addCartItemRequest.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items")
    public ResponseEntity<Void> updateCartItemQuantity(@Valid @RequestBody UpdateCartItemRequest updateCartItemRequest) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cartService.updateCartItemQuantity(user.getUser().getId(), updateCartItemRequest.getProductId(), updateCartItemRequest.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long id) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cartService.deleteCartItem(user.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems() {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(cartService.getCartItems(user.getUser().getId()));
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCart() {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cartService.clearCart(user.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
