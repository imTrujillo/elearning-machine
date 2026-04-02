package com.learning_engine.dto;

import java.util.List;

public record WooProductDto (
        Long id,
        String name,
        String regular_price,
        String status,
        List<WooCategory> categories
) {
    public record WooCategory (Long id, String name, String slug){}
}
