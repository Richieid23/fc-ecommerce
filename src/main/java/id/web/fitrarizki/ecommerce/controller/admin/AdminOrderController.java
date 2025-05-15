package id.web.fitrarizki.ecommerce.controller.admin;

import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.order.OrderResponse;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.OrderService;
import id.web.fitrarizki.ecommerce.util.PageUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping()
    public ResponseEntity<PaginatedResponse<OrderResponse>> findOrdersByUserId(@RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "10") int size,
                                                                               @RequestParam(defaultValue = "order_id,desc") String[] sort) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Sort.Order> sortOrder = PageUtil.parseSortOrderRequest(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder));

        return ResponseEntity.ok(orderService.getPaginatedOrdersByUserId(user.getUser().getId(), pageable));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable Long orderId) {
        UserInfo user = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(OrderResponse.fromOrder(orderService.getOrderById(orderId)));
    }
}
