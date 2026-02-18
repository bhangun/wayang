package tech.kayys.wayang.node.websearch.api;

import java.util.Locale;

public enum SearchCapability {
    TEXT_SEARCH, IMAGE_SEARCH, VIDEO_SEARCH, NEWS_SEARCH,
    ACADEMIC_SEARCH, SHOPPING_SEARCH, LOCAL_SEARCH, CODE_SEARCH,
    WEB_SCRAPING, SEMANTIC_SEARCH, HYBRID_SEARCH;
    
    public static SearchCapability fromString(String type) {
        if (type == null || type.isBlank()) {
            return TEXT_SEARCH;
        }

        return switch(type.trim().toLowerCase(Locale.ROOT)) {
            case "text" -> TEXT_SEARCH;
            case "image" -> IMAGE_SEARCH;
            case "video" -> VIDEO_SEARCH;
            case "news" -> NEWS_SEARCH;
            default -> TEXT_SEARCH;
        };
    }
}
