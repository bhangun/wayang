package tech.kayys.gamelan.executor.camel.blockchain;

import java.time.Instant;

record IPFSUploadResult(
        String cid,
        String fileName,
        long size,
        String url,
        Instant uploadedAt) {
}