package org.ai.shopping.model;

import lombok.Data;

@Data
public class Product {

    private String title;
    private String brand;
    private double price;
    private double rating;
    private int reviews;
    private String source;
    private String link;
}