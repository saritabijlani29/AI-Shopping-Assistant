package org.ai.shopping.controller;

import lombok.RequiredArgsConstructor;
import org.ai.shopping.model.Product;
import org.ai.shopping.service.ProductFilterService;
import org.ai.shopping.service.ProductParserService;
import org.ai.shopping.service.ProductRetrievalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductRetrievalService retrievalService;
    private final ProductParserService parserService;
    private final ProductFilterService filterService;

    @GetMapping("/search")
    public List<Product> search(@RequestParam String query) {

        String response = retrievalService.searchProducts(query);
        List<Product>  parsedProduct =  parserService.parse(response);
        return parsedProduct;
        //return filterService.filter(parsedProduct);
    }

}