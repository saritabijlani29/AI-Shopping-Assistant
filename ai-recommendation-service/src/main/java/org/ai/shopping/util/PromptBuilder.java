package org.ai.shopping.util;

import org.ai.shopping.model.Product;

import java.util.List;

public class PromptBuilder {

    public static String buildPrompt(List<Product> products, String query) {

        String productText = products.stream()
                .map(p -> """
                    Title: %s
                    Price: %s
                    Rating: %s
                    Reviews: %s
                    Link: %s
                    """.formatted(
                        p.getTitle(),
                        p.getPrice(),
                        p.getRating(),
                        p.getReviews(),
                        p.getLink()
                ))
                .reduce("", (a, b) -> a + "\n" + b);

        return """
        You are an ecommerce product expert.

        User request:
        %s

        Analyze the following products and recommend the best 10.

        Consider:
        - rating
        - review count
        - value for money
        - quality

        Products:
        %s
        """.formatted(query, productText);
    }
}
