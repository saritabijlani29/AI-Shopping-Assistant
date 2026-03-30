package org.ai.shopping.controller;

import lombok.RequiredArgsConstructor;
import org.ai.shopping.service.CacheService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.ai.shopping.model.Product;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @PostMapping("/products")
    public Mono<Void> save(@RequestParam String query,
                           @RequestBody List<Product> products) {
        return cacheService.save(query, products);
    }

    @GetMapping("/products")
    public Mono<List<Product>> get(@RequestParam String query) {
        return cacheService.get(query);
    }

    @GetMapping("/all")
    public Mono<Map<Object, Object>> getAll() {
        return cacheService.getAll();
    }


}
