package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.cart.CartItemResponse;
import id.web.fitrarizki.ecommerce.exception.BadRequestException;
import id.web.fitrarizki.ecommerce.exception.ForbiddenAccessException;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.Cart;
import id.web.fitrarizki.ecommerce.model.CartItem;
import id.web.fitrarizki.ecommerce.model.Product;
import id.web.fitrarizki.ecommerce.repository.CartItemRepository;
import id.web.fitrarizki.ecommerce.repository.CartRepository;
import id.web.fitrarizki.ecommerce.repository.ProductRepository;
import id.web.fitrarizki.ecommerce.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void addCartItem(Long userId, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getUserId().equals(userId)) {
            throw new BadRequestException("Cannot add your own product to cart");
        }

        Optional<CartItem> cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (cartItem.isPresent()) {
            cartItem.get().setQuantity(cartItem.get().getQuantity() + quantity);
            cartItemRepository.save(cartItem.get());
        } else {
            cartItemRepository.save(CartItem.builder()
                    .cartId(cart.getId())
                    .productId(productId)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build());
        }

    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Long userId, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Override
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCartId().equals(cart.getId())) {
            throw new ForbiddenAccessException("Cannot update cart item of another user");
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cartItemRepository.deleteAllByCartId(cart.getId());
    }

    @Override
    public List<CartItemResponse> getCartItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = cartItems.stream().map(CartItem::getProductId).toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        return cartItems.stream().map(cartItem -> CartItemResponse.fromCartItemAndProduct(cartItem, productMap.get(cartItem.getProductId()))).toList();
    }
}
