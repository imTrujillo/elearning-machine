package com.learning_engine.integration;

/**
 * Con enlaces permanentes "simples", WordPress no expone /wp-json/... y responde 404.
 * La ruta compatible es /index.php?rest_route=/wp/v2/...
 */
public final class WordpressRestUri {

    private WordpressRestUri() {
    }

    /**
     * @param wpJsonPath ruta tipo /wp-json/wp/v2/posts?per_page=100
     */
    public static String resolve(String wpJsonPath, boolean plainRest) {
        if (!plainRest || wpJsonPath == null || !wpJsonPath.startsWith("/wp-json")) {
            return wpJsonPath;
        }
        int q = wpJsonPath.indexOf('?');
        String path = q >= 0 ? wpJsonPath.substring(0, q) : wpJsonPath;
        String query = q >= 0 ? wpJsonPath.substring(q + 1) : "";
        String route = path.substring("/wp-json".length());
        if (route.isEmpty()) {
            route = "/";
        }
        StringBuilder sb = new StringBuilder("/index.php?rest_route=").append(route);
        if (!query.isEmpty()) {
            sb.append('&').append(query);
        }
        return sb.toString();
    }
}
