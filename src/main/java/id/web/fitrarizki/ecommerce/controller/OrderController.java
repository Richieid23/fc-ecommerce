package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.order.CheckOutRequest;
import id.web.fitrarizki.ecommerce.dto.order.OrderItemResponse;
import id.web.fitrarizki.ecommerce.dto.order.OrderResponse;
import id.web.fitrarizki.ecommerce.exception.BadRequestException;
import id.web.fitrarizki.ecommerce.model.Order;
import id.web.fitrarizki.ecommerce.model.OrderStatus;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.OrderService;
import id.web.fitrarizki.ecommerce.util.PageUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckOutRequest checkOutRequest) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        checkOutRequest.setUserId(user.getUser().getId());

        return ResponseEntity.ok(orderService.checkout(checkOutRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Order order = orderService.getOrderById(id);
        if (!order.getUserId().equals(user.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(OrderResponse.fromOrder(order));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<OrderResponse>> getOrdersByUserId(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "20") int size,
                                                                              @RequestParam(defaultValue = "id,desc") String[] sort) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Sort.Order> orders = PageUtil.parseSortOrderRequest(sort);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(orders));

        return ResponseEntity.ok(orderService.getPaginatedOrdersByUserId(user.getUser().getId(), pageRequest));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderId(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderItemsByOrderId(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        OrderStatus orderStatus = null;
        try {
            orderStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unrecognized order status: " + status);
        }
        orderService.updateOrderStatus(id, orderStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calculateOrderTotal(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.calculateOrderTotal(id));
    }
}
