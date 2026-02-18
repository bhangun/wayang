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

package tech.kayys.wayang.control.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

/**
 * Integration Pattern entity - common patterns for connecting systems.
 */
@Entity
@Table(name = "integration_patterns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationPattern extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String patternId;

    @Column(nullable = false)
    public String name;

    public String description;

    @Column(nullable = false)
    public String patternType; // e.g., "pub-sub", "request-reply", "point-to-point"

    @Column(name = "configuration_schema", length = 4000)
    public String configurationSchema;

    public String version;
}
