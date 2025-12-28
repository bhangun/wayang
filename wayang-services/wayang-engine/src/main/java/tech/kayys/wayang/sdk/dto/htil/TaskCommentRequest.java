package tech.kayys.wayang.sdk.dto.htil;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SDK DTO for Task Comment Request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskCommentRequest {
    private String comment;
    private String userId;

    public TaskCommentRequest() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
