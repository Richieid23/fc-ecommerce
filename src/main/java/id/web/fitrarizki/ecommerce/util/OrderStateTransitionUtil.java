package id.web.fitrarizki.ecommerce.util;

import id.web.fitrarizki.ecommerce.model.OrderStatus;

import java.util.EnumMap;
import java.util.Set;

public class OrderStateTransitionUtil {
    private static final EnumMap<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        VALID_TRANSITIONS.put(OrderStatus.PENDING, Set.of(OrderStatus.CANCELLED, OrderStatus.PAID, OrderStatus.PAYMENT_FAILED));
        VALID_TRANSITIONS.put(OrderStatus.PAID, Set.of(OrderStatus.SHIPPING));
        VALID_TRANSITIONS.put(OrderStatus.SHIPPING, Set.of());
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
        VALID_TRANSITIONS.put(OrderStatus.PAYMENT_FAILED, Set.of());
    }

    public static boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return VALID_TRANSITIONS.get(from).contains(to);
    }
}
