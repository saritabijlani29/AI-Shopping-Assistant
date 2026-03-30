package org.ai.shopping.controller;

import lombok.RequiredArgsConstructor;
import org.ai.shopping.service.AiRecommendationService;
import org.ai.shopping.Client.ProductClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class RecommendationController {

    private final AiRecommendationService aiService;
    private final ProductClient productClient;
    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    @GetMapping("/recommend")
    public Mono<String> recommend(@RequestParam String query) {
        String requestId = java.util.UUID.randomUUID().toString();

        return productClient.searchProducts(query)
                .flatMap(products -> {
                    if (products.isEmpty()) {
                        log.info("No products found, skipping LLM");
                        return Mono.just("""
                                    We are having trouble fetching live products.
                                
                                    Popular categories you can explore:
                                    - Mobile Covers
                                    - Accessories
                                    - Best Sellers
                                
                                    Please try again in a few moments.
                                """);

                    }
                    log.info("Products fetched: {} " , products.size());
                    return aiService.recommendTopProducts(products, query)
                            .doOnSuccess(res ->
                                    log.info("LLM success for requestId {}", requestId)
                            )
                            .doOnError(err ->
                                    log.info("LLM Failed for requestId {} with exception {} ", requestId, err.getMessage())
                            );
                })
                .doOnSuccess(res ->
                        log.info("Controller success for requestId {}", requestId)
                )
                .doOnError(error ->
                        log.info("Controller Failed for requestId {} with exception {} ", requestId, error.getMessage())
                )
                .onErrorResume(error -> {
                    log.info("Returning fallback due to error");
                    return Mono.just("Something went wrong. Please try again later.");
                });
    }
}