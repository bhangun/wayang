package tech.kayys.wayang.rag;

import java.util.Map;

record DocumentSource(
        SourceType type,
        String path,
        String content,
        Map<String, String> metadata) {
}