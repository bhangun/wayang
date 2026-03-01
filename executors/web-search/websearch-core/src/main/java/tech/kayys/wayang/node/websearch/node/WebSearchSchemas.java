package tech.kayys.wayang.node.websearch.node;

public class WebSearchSchemas {
    public static final String WEB_SEARCH_CONFIG = """
            {
              "type": "object",
              "properties": {
                "query": {
                  "type": "string",
                  "description": "The search query (templateable)",
                  "x-component": "textarea"
                },
                "searchType": {
                  "type": "string",
                  "description": "Type of search (text, news, image, video)",
                  "default": "text",
                  "enum": ["text", "news", "image", "video"]
                },
                "maxResults": {
                  "type": "integer",
                  "description": "Maximum number of results to return",
                  "default": 10,
                  "minimum": 1,
                  "maximum": 50
                },
                "providers": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  },
                  "description": "Preferred search providers (google, bing, duckduckgo)",
                  "default": ["google", "bing", "duckduckgo"]
                },
                "safeSearch": {
                  "type": "boolean",
                  "description": "Whether to enable safe search",
                  "default": true
                }
              },
              "required": ["query"]
            }
            """;
}
