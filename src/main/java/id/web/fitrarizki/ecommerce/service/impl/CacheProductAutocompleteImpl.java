package id.web.fitrarizki.ecommerce.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import id.web.fitrarizki.ecommerce.config.AppProp;
import id.web.fitrarizki.ecommerce.service.CacheProductAutocomplete;
import id.web.fitrarizki.ecommerce.service.CacheService;
import id.web.fitrarizki.ecommerce.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheProductAutocompleteImpl implements CacheProductAutocomplete {

    private final SearchService searchService;
    private final CacheService cacheService;
    private final AppProp appProp;

    @Override
    public List<String> getAutocomplete(String query) {
        String cacheKey = "product:autocomplete:" + query;

        return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        }).orElseGet(() -> {
            List<String> autocomplete = searchService.getAutocomplete(query);
            cacheService.set(cacheKey, autocomplete, appProp.getRedis().getSuggestionCacheTtl());
            return autocomplete;
        });
    }

    @Override
    public List<String> getNgramAutocomplete(String query) {
        String cacheKey = "product:ngram:autocomplete:" + query;

        return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        }).orElseGet(() -> {
            List<String> autocomplete = searchService.getNgramAutocomplete(query);
            cacheService.set(cacheKey, autocomplete, appProp.getRedis().getSuggestionCacheTtl());
            return autocomplete;
        });
    }

    @Override
    public List<String> getFuzzyAutocomplete(String query) {
        String cacheKey = "product:fuzzy:autocomplete:" + query;

        return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        }).orElseGet(() -> {
            List<String> autocomplete = searchService.getFuzzyAutocomplete(query);
            cacheService.set(cacheKey, autocomplete, appProp.getRedis().getSuggestionCacheTtl());
            return autocomplete;
        });
    }

    @Override
    public List<String> combinedAutocomplete(String query) {
        String cacheKey = "product:combine:autocomplete:" + query;

        return cacheService.get(cacheKey, new TypeReference<List<String>>() {
        }).orElseGet(() -> {
            List<String> autocomplete = searchService.combinedAutocomplete(query);
            cacheService.set(cacheKey, autocomplete, appProp.getRedis().getSuggestionCacheTtl());
            return autocomplete;
        });
    }
}
