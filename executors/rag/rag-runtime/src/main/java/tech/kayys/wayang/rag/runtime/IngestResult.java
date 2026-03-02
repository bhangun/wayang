package tech.kayys.wayang.rag.runtime;
import tech.kayys.wayang.rag.plugin.api.*;
import tech.kayys.wayang.rag.core.*;

import java.util.Map;

record IngestResult(
                boolean success,
                int documentsIngested,
                int segmentsCreated,
                long durationMs,
                String message) {
}