package id.web.fitrarizki.ecommerce.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Nama kategori tidak boleh kosong")
    @Size(min = 3, max = 255, message = "Nama kategori harus antara 3 dan 255 karakter")
    private String name;

    @NotNull(message = "Deskripsi kategori tidak boleh null")
    @Size(max = 1000, message = "Deskripsi kategori tidak boleh lebih dari 1000 karakter")
    private String description;
}
