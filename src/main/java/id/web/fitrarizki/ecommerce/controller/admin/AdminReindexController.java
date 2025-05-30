package id.web.fitrarizki.ecommerce.controller.admin;

import id.web.fitrarizki.ecommerce.service.BulkReindexService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/admin/reindex")
@SecurityRequirement(name = "Bearer")
@RequiredArgsConstructor
public class AdminReindexController {
    private final BulkReindexService bulkReindexService;

    @PostMapping("/products")
    public ResponseEntity<String> reindexAllProducts() {
        try {
            bulkReindexService.reindexAllProducts();
            return ResponseEntity.ok("Reindex completed");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error while reindex. " + e.getMessage());
        }
    }
}
