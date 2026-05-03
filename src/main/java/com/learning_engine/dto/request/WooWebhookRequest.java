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

    public static WooWebhookRequest fromActivation(Long wooOrderId, String customerEmail){
        return new WooWebhookRequest(
                wooOrderId,
                "completed",
                customerEmail,
                List.of()
        );
    }
}
