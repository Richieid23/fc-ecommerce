package id.web.fitrarizki.ecommerce.service;

import java.util.List;

public interface CacheProductAutocomplete {
    List<String> getAutocomplete(String query);
    List<String> getNgramAutocomplete(String query);
    List<String> getFuzzyAutocomplete(String query);
    List<String> combinedAutocomplete(String query);
}
