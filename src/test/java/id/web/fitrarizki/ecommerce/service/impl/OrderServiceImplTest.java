package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.order.CheckOutRequest;
import id.web.fitrarizki.ecommerce.dto.order.OrderResponse;
import id.web.fitrarizki.ecommerce.dto.order.ShippingRateResponse;
import id.web.fitrarizki.ecommerce.dto.payment.PaymentResponse;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.*;
import id.web.fitrarizki.ecommerce.repository.*;
import id.web.fitrarizki.ecommerce.service.InventoryService;
import id.web.fitrarizki.ecommerce.service.PaymentService;
import id.web.fitrarizki.ecommerce.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserAddressRepository userAddressRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ShippingService shippingService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CheckOutRequest checkoutRequest;
    private List<CartItem> cartItems;
    private UserAddress userAddress;
    private Product product;
    private UserAddress sellerAddress;
    private User seller;
    private User buyer;

    @BeforeEach
    void setUp() {
        checkoutRequest = new CheckOutRequest();
        checkoutRequest.setUserId(1L);
        checkoutRequest.setUserAddressId(1L);
        checkoutRequest.setCartItemIds(Arrays.asList(1L, 2L));

        cartItems = new ArrayList<>();
        CartItem cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setProductId(1L);
        cartItem1.setQuantity(2);
        cartItem1.setPrice(new BigDecimal("100.00"));
        cartItems.add(cartItem1);

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setProductId(2L);
        cartItem2.setQuantity(1);
        cartItem2.setPrice(new BigDecimal("50.00"));
        cartItems.add(cartItem2);

        userAddress = new UserAddress();
        userAddress.setId(1L);

        seller = new User();
        seller.setId(1L);
        buyer = new User();
        buyer.setId(2L);

        product = new Product();
        product.setId(1L);
        product.setWeight(new BigDecimal("0.5"));
        product.setUserId(seller.getId());

        sellerAddress = new UserAddress();
        sellerAddress.setId(2L);
        sellerAddress.setUserId(seller.getId());
    }

    @Test
    void testCheckout_WhenCartIsEmpty() {
        when(cartItemRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.checkout(checkoutRequest);
        });
    }

    @Test
    void testCheckout_SuccessfulCheckout() {
        // Arrange
        when(cartItemRepository.findAllById(anyList())).thenReturn(cartItems);
        when(userAddressRepository.findById(anyLong())).thenReturn(Optional.of(userAddress));
        when(inventoryService.checkInventoryAvailability(anyMap())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
//        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
//        when(userAddressRepository.findByUserIdAndIsDefaultTrue(anyLong())).thenReturn(
//                Optional.of(sellerAddress));

        ShippingRateResponse shippingRateResponse = new ShippingRateResponse();
        shippingRateResponse.setShippingFee(new BigDecimal("10.00"));
//        when(shippingService.calculateShippingRate(any())).thenReturn(shippingRateResponse);

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setXenditInvoiceId("payment123");
        paymentResponse.setXenditInvoiceStatus("PENDING");
        paymentResponse.setXenditPaymentUrl("http://payment.url");
        when(paymentService.create(any())).thenReturn(paymentResponse);

        // Act
        OrderResponse result = orderService.checkout(checkoutRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("payment123", result.getXenditInvoiceId());
        assertEquals("http://payment.url", result.getPaymentUrl());

//        verify(cartItemRepository).findAllById(checkoutRequest.getCartItemIds());
//        verify(userAddressRepository).findById(checkoutRequest.getUserAddressId());
//        verify(inventoryService).checkInventoryAvailability(anyMap());
//        verify(orderRepository, times(2)).save(any(Order.class));
//        verify(orderItemRepository).saveAll(anyList());
//        verify(cartItemRepository).deleteAll(cartItems);
//        verify(shippingService, times(2)).calculateShippingRate(
//                any());  // Verify called twice for two cart items
//        verify(paymentService).create(any());
//        verify(inventoryService).decreaseInventory(anyMap());
//        verify(userAddressRepository, times(2)).findByUserIdAndIsDefaultTrue(
//                anyLong());  // Verify called twice for two products
    }

}