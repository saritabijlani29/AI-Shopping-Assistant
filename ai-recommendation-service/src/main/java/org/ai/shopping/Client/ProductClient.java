package org.ai.shopping.Client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import jakarta.annotation.PostConstruct;
import org.ai.shopping.model.Product;
import org.ai.shopping.service.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
public class ProductClient {

    private final WebClient.Builder builder;
    private WebClient webClient;
    private final CircuitBreaker circuitBreaker;
   // private final Map<String, List<Product>> cache = new ConcurrentHashMap<>();
    private final CacheClient cacheClient;

    @Value("${services.product.base-url}")
    private String baseUrl;

    @Value("${services.product.search-endpoint}")
    private String searchEndpoint;

    @Value("${services.product.timeout}")
    private int timeout;

    public ProductClient(WebClient.Builder builder, CircuitBreaker circuitBreaker, CacheClient cacheClient) {
        this.builder = builder;
        this.circuitBreaker = circuitBreaker;
        this.cacheClient = cacheClient;
    }

    @PostConstruct
    public void init() {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<List<Product>> searchProducts(String query) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(searchEndpoint)
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(
                                        new RuntimeException("Product Service Error: " + error)
                                ))
                )
                .bodyToFlux(Product.class)
                .collectList()
                .flatMap(products -> {
                    if (!products.isEmpty()) {
                        return cacheClient.saveCache(query,products)
                                .doOnSuccess(v -> System.out.println("✅ Cache saved"))
                                .thenReturn(products);
                    }
                    return Mono.just(products);
                })
                // ⏱ timeout
                .timeout(Duration.ofSeconds(timeout))

                // 🔁 retry with backoff
                .retryWhen(
                        Retry.backoff(0, Duration.ofMillis(500))
                                .doBeforeRetry(retrySignal ->
                                        System.out.println("🔁 Retry attempt: " + (retrySignal.totalRetries() + 1))
                                )
                )

                // ⚡ circuit breaker (reactive way)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))

                // 🛟 fallback
                .onErrorResume(ex -> {
                    System.out.println("🔥 Fallback triggered: " + ex.getMessage());
                    System.out.println("❌ Product service failed: " + ex.getMessage());

//                    List<Product> cached = cache.get(query);
//
//                    if (cached != null) {
//                        System.out.println("⚡ Returning cached products");
//                        return Mono.just(cached);
//                    }
//                    System.out.println("❌ Product service failed: " + error.getMessage());
                    return cacheClient.getCache(query);
                });
    }
}