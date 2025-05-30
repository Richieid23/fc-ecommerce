package id.web.fitrarizki.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchResponse<T> {
    private List<T> data;
    private long totalHits;
    private Map<String, List<FacetEntry>> facets;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class FacetEntry {
        private String key;
        private Long docCount;
    }
}
