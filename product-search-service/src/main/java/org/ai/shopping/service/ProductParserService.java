package org.ai.shopping.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ai.shopping.exception.ProductParsingException;
import org.ai.shopping.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductParserService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Product> parse(String json) {

        List<Product> products = new ArrayList<>();

        try {

            JsonNode root = objectMapper.readTree(json);

            JsonNode results = root.path("shopping_results");

            if (!results.isArray()) {
                log.warn("shopping_results not found in response");
                return products;
            }

            for (JsonNode item : results) {

                Product p = new Product();

                p.setTitle(item.path("title").asText(""));
                p.setLink(item.path("product_link").asText(""));
                p.setSource(item.path("source").asText(""));

                p.setRating(item.path("rating").asDouble(0));
                p.setReviews(item.path("reviews").asInt(0));

                p.setPrice(parsePrice(item.path("price").asText()));

                products.add(p);
            }

            log.info("Successfully parsed {} products", products.size());

            return products;

        } catch (Exception e) {

            log.error("Error parsing product response", e);

            throw new ProductParsingException(
                    "Failed to parse product JSON", e
            );
        }
    }

    private double parsePrice(String priceText) {

        try {

            if (priceText == null || priceText.isEmpty()) {
                return 0;
            }

            return Double.parseDouble(
                    priceText.replaceAll("[^0-9.]", "")
            );

        } catch (Exception e) {

            log.warn("Invalid price format: {}", priceText);

            return 0;
        }
    }
}