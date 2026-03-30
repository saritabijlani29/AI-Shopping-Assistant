package org.ai.shopping.Client;

import jakarta.annotation.PostConstruct;
import org.ai.shopping.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CacheClient {

    private final WebClient.Builder builder;
    private WebClient webClient;

    @Value("${services.cache.endpoint}")
    private String cacheEndpoint;

    @Value("${services.cache.base-url}")
    private String baseUrl;

    public CacheClient(WebClient.Builder builder) {
        this.builder = builder;
    }

    @PostConstruct
    public void init() {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<List<Product>> getCache(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(cacheEndpoint)
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .bodyToFlux(Product.class)
                .collectList();
    }

    public Mono<Void> saveCache(String query, List<Product> products) {
        System.out.println("reached at the redis to save the data.....");
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(cacheEndpoint)
                        .queryParam("query", query)
                        .build())
                .bodyValue(products)
                .retrieve()
                // ✅ log response status
                .toBodilessEntity()
                .doOnNext(response ->
                        System.out.println("✅ Cache saved successfully. Status: " + response.getStatusCode())
                )

                // ✅ log errors
                .doOnError(error ->
                        System.out.println("❌ Cache save failed: " + error.getMessage())
                )

                .then();
    }
}
