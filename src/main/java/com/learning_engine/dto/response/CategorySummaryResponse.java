package com.learning_engine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CategorySummaryResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name,
        @JsonProperty("slug") String slug
) {}
