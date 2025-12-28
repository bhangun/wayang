package tech.kayys.wayang.sdk.dto.htil;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * SDK DTO for Task Completion Request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskCompletionRequest {
    private Map<String, Object> output;
    private String comments;

    public TaskCompletionRequest() {
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
