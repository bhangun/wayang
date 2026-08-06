package tech.kayys.wayang.cli.skill;

import picocli.CommandLine.Option;
import tech.kayys.wayang.gollek.sdk.AgentSkillQuery;
import tech.kayys.wayang.gollek.sdk.AgentSkillState;

final public class WayangSkillQueryOptions {

    @Option(names = "--surface", description = "Filter skills to a product surface.")
    public String surfaceId;

    @Option(names = "--profile", description = "Filter skills through a product profile.")
    public String profileId;

    @Option(names = "--category", description = "Filter skills to one category.")
    public String category;

    @Option(names = "--source", description = "Filter skills to one source, for example rag or mcp.")
    public String source;

    @Option(names = "--state", description = "Filter skills by state: active, preview, disabled, or deprecated.")
    public String state;

    @Option(names = "--tag", description = "Filter skills by tag.")
    public String tag;

    @Option(names = "--input", description = "Filter skills by input key.")
    public String inputKey;

    @Option(names = "--output", description = "Filter skills by output key.")
    public String outputKey;

    public AgentSkillQuery toQuery(String skillId) {
        return new AgentSkillQuery(
                surfaceId,
                profileId,
                category,
                source,
                state == null || state.isBlank() ? null : AgentSkillState.from(state),
                skillId,
                tag,
                inputKey,
                outputKey);
    }
}
