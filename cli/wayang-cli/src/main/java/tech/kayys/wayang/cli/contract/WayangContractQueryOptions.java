package tech.kayys.wayang.cli.contract;

import picocli.CommandLine.Option;
import tech.kayys.wayang.gollek.sdk.WayangContractQuery;

final public class WayangContractQueryOptions {

    @Option(names = "--schema", description = "Filter by schema, for example wayang.run.lifecycle.")
    public String schema;

    @Option(names = "--envelope", description = "Filter by envelope, for example run-preview.")
    public String envelope;

    @Option(names = "--command-id", description = "Filter by command id, for example run-dry-json.")
    public String commandId;

    @Option(names = "--domain", description = "Filter by contract domain, for example lifecycle.")
    public String domain;

    @Option(names = "--json-schema-id", description = "Filter by JSON Schema id, for example urn:wayang:contract:wayang.run.planning:v1:run-preview.")
    public String jsonSchemaId;

    public WayangContractQuery toQuery() {
        return WayangContractQuery.of(schema, envelope, commandId, domain, jsonSchemaId);
    }
}
