package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.exception.InventoryException;
import id.web.fitrarizki.ecommerce.model.Product;
import id.web.fitrarizki.ecommerce.repository.ProductRepository;
import id.web.fitrarizki.ecommerce.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public boolean checkInventoryAvailability(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPesimisticLock(entry.getKey()).orElseThrow(() -> new InventoryException("Product not found"));
            if (product.getStockQuantity() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void increaseInventory(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPesimisticLock(entry.getKey()).orElseThrow(() -> new InventoryException("Product not found"));
            product.setStockQuantity(product.getStockQuantity() + entry.getValue());
            productRepository.save(product);
        }

    }

    @Override
    @Transactional
    public void decreaseInventory(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPesimisticLock(entry.getKey()).orElseThrow(() -> new InventoryException("Product not found"));

            if (product.getStockQuantity() < entry.getValue() ) {
                throw new InventoryException("Product stock quantity is less than required quantity");
            }

            product.setStockQuantity(product.getStockQuantity() - entry.getValue());
            productRepository.save(product);
        }
    }
}
