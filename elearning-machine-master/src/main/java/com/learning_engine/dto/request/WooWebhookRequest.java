package com.learning_engine.dto.request;

import java.util.List;

public record WooWebhookRequest(
        Long id,
        String status,
        String customerEmail,
        List<WooOrderItem> lineItems
) {
    public record WooOrderItem(
            Long productId,
            String name,
            Integer quantity
    ){ }
}
