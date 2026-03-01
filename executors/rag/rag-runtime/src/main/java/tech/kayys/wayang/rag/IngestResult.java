package tech.kayys.wayang.rag;

import java.util.Map;

record IngestResult(
                boolean success,
                int documentsIngested,
                int segmentsCreated,
                long durationMs,
                String message) {
}