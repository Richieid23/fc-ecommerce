package id.web.fitrarizki.ecommerce.service;

import java.util.Map;

public interface InventoryService {
    boolean checkInventoryAvailability(Map<Long, Integer> productQuantities);
    void increaseInventory(Map<Long, Integer> productQuantities);
    void decreaseInventory(Map<Long, Integer> productQuantities);
}
