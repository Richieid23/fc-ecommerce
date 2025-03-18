package id.web.fitrarizki.ecommerce.util;

import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {
    public static List<Sort.Order> parseSortOrderRequest(String[] sort) {
        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sort) {
            if (s.contains(",")) {
                String[] split = s.split(",");
                orders.add(new Sort.Order(getSortDirection(split[1]), split[0]));
            }
        }
        return orders;
    }

    private static Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    public static <T> PaginatedResponse<T> getPaginatedResponse(Page<T> page) {
        PaginatedResponse<T> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setData(page.getContent());
        paginatedResponse.setPageNo(page.getNumber());
        paginatedResponse.setPageSize(page.getSize());
        paginatedResponse.setTotalElements(page.getTotalElements());
        paginatedResponse.setTotalPages(page.getTotalPages());
        paginatedResponse.setFirst(page.isFirst());
        paginatedResponse.setLast(page.isLast());

        return paginatedResponse;
    }
}
