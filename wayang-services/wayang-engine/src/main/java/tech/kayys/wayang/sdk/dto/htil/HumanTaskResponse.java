package tech.kayys.wayang.sdk.dto.htil;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * SDK DTO for Human Task Response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HumanTaskResponse {
    private String taskId;
    private String status;
    private String priority;
    private String assignee;
    private String description;
    private Map<String, Object> input;
    private Map<String, Object> output;

    public HumanTaskResponse() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }
}
