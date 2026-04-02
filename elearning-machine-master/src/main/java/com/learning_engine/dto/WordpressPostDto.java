package com.learning_engine.dto;

import java.util.Map;

public record WordpressPostDto (
        Long id,
        String slug,
        WordpressTitle title,
        WordpressContent content,
        String jetpack_featured_media_url,
        Map<String, Object> acf
){
    public record WordpressTitle(String rendered) {}
    public record WordpressContent(String rendered) {}
}
