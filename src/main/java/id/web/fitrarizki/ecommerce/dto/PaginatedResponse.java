package id.web.fitrarizki.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
}
