package tech.kayys.wayang.agent.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Configuration for an Evaluator Agent. */
public class EvaluatorAgentConfig extends AgentConfig {
    @JsonProperty("candidateOutput")
    private String candidateOutput;

    @JsonProperty("output")
    private String output;

    @JsonProperty("result")
    private String result;

    @JsonProperty("content")
    private String content;

    @JsonProperty("criteria")
    private String criteria;

    @JsonProperty("preferredProvider")
    private String preferredProvider;

    public EvaluatorAgentConfig() {
        super();
    }

    public String getCandidateOutput() {
        return candidateOutput;
    }

    public void setCandidateOutput(String candidateOutput) {
        this.candidateOutput = candidateOutput;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getPreferredProvider() {
        return preferredProvider;
    }

    public void setPreferredProvider(String preferredProvider) {
        this.preferredProvider = preferredProvider;
    }
}
