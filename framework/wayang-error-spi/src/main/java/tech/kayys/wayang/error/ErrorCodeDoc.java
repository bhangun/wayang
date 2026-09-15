/*
 * PolyForm Noncommercial License 1.0.0
 *
 * Copyright (c) 2026 Kayys.tech
 *
 * This software is licensed for non-commercial use only.
 * You may use, modify, and distribute this software for personal,
 * educational, or research purposes.
 *
 * Commercial use, including SaaS or revenue-generating services,
 * requires a separate commercial license from Kayys.tech.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 *
 * @author Bhangun
 */

package tech.kayys.wayang.error;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility to generate Markdown documentation for {@link ErrorCode}.
 */
public final class ErrorCodeDoc {

    private ErrorCodeDoc() {
    }

    public static String toMarkdown() {
        String nl = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append("# Wayang Error Codes").append(nl).append(nl);
        builder.append("Generated from `ErrorCode` at build time.").append(nl).append(nl);

        for (Map.Entry<String, List<ErrorCode>> entry : categoriesInOrder().entrySet()) {
            List<ErrorCode> codes = entry.getValue();
            if (codes.isEmpty()) {
                continue;
            }
            builder.append("## ").append(codes.get(0).getCategory().getPrefix()).append(nl).append(nl);
            builder.append("| Code | HTTP | Retryable | Message |").append(nl);
            builder.append("| --- | --- | --- | --- |").append(nl);
            for (ErrorCode code : codes) {
                builder.append("| ")
                        .append(code.getCode()).append(" | ")
                        .append(code.getHttpStatus()).append(" | ")
                        .append(code.isRetryable()).append(" | ")
                        .append(code.getDefaultMessage())
                        .append(" |")
                        .append(nl);
            }
            builder.append(nl);
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.print(toMarkdown());
    }

    private static Map<String, List<ErrorCode>> categoriesInOrder() {
        Map<String, List<ErrorCode>> categories = new LinkedHashMap<>();
        for (ErrorCode errorCode : ErrorCode.values()) {
            categories.computeIfAbsent(errorCode.getCategory().name(), ignored -> new ArrayList<>()).add(errorCode);
        }
        return categories;
    }
}
