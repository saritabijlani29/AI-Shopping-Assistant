package org.ai.shopping.service;

import org.ai.shopping.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductFilterService {

    public List<Product> filter(List<Product> product){

        return product.stream()
                .filter(p -> p.getReviews() >= 3.5)
                .filter(p -> p.getReviews() >= 40)
                .limit(20)
                .toList();
    }
}
