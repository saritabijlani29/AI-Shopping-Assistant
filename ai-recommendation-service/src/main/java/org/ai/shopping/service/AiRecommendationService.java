package org.ai.shopping.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.ai.shopping.controller.RecommendationController;
import org.ai.shopping.model.LlmRequest;
import org.ai.shopping.model.LlmResponse;
import org.ai.shopping.model.Product;
import org.ai.shopping.util.PromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AiRecommendationService {

    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(AiRecommendationService.class);

    @Value("${github.token}")
    private String githubToken;

    @Value("${llm.model}")
    private String model;

    public AiRecommendationService(WebClient.Builder builder,
                                   @Value("${llm.endpoint}") String endpoint) {

        this.webClient = builder.baseUrl(endpoint).build();
    }

    public Mono<String> recommendTopProducts(List<Product> products, String query){

        if (products == null || products.isEmpty()) {
            return Mono.just("No products available at the moment. Please try again later.");
        }

        String prompt = PromptBuilder.buildPrompt(products, query);
        log.info("prompt build for AI analysis {} ", prompt);
        LlmRequest request = new LlmRequest(
                model,
                List.of(
                        new LlmRequest.Message("system", "You are an ecommerce shopping expert."),
                        new LlmRequest.Message("user", prompt)
                ),
                0.2
        );

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + githubToken)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LlmResponse.class)
                .map(response -> response.choices.get(0).message.content)

                .doOnSuccess(v ->
                        log.info("AI recommendation is SUCCESS")
                )
                .doOnError(e ->
                        log.info("AI recommendation failed: {} " , e.getMessage())
                )
                // error handling
                .onErrorResume(error -> {
                    log.info("AI service failed: {} " , error.getMessage());
                    return Mono.just("AI recommendation failed");
                });
    }
}
