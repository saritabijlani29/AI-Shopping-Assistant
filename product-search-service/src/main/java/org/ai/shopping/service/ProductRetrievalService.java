package org.ai.shopping.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ProductRetrievalService {

    private WebClient webClient;

    @Value("${serpapi.key}")
    private String apiKey;

    public ProductRetrievalService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://serpapi.com").build();
    }

    public String searchProducts(String query) {

        return webClient.get().
                uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("engine", "google_shopping")
                        .queryParam("q", query)
                        .queryParam("gl", "in")
                        .queryParam("hl", "en")
                        .queryParam("no_cache", "true")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
