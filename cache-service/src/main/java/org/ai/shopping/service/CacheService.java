package org.ai.shopping.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ai.shopping.model.Product;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final String KEY = "products";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Save cache with TTL
    public Mono<Void> save(String query, List<Product> products) {
        return redisTemplate.opsForHash()
                .put(KEY, query, products)
                .then(redisTemplate.expire(KEY, Duration.ofMinutes(10)))
                .then();
    }

    // ✅ Get cache
    public Mono<List<Product>> get(String query) {
        return redisTemplate.opsForHash()
                .get(KEY, query)
                .map(obj -> objectMapper.convertValue(
                        obj, new TypeReference<List<Product>>() {}
                ))
                .defaultIfEmpty(List.of());
    }

    // ✅ Get all cache
    public Mono<Map<Object, Object>> getAll() {
        return redisTemplate.opsForHash().entries(KEY)
                .collectMap(
                entry -> entry.getKey(),
                entry -> entry.getValue()
        );
    }
}