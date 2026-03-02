package tech.kayys.wayang.rag.runtime;
import tech.kayys.wayang.rag.plugin.api.*;
import tech.kayys.wayang.rag.core.*;

import java.util.Map;

record DocumentSource(
        SourceType type,
        String path,
        String content,
        Map<String, String> metadata) {
}