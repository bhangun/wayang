package tech.kayys.wayang.engine;

import java.util.List;

import tech.kayys.wayang.sdk.dto.WorkflowRunResponse;

/*
Paginated workflow run response.
 */
public class PagedWorkflowRunResponse {
    private List<WorkflowRunResponse> runs;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    // Getters and setters
    public List<WorkflowRunResponse> getRuns() {
        return runs;
    }

    public void setRuns(List<WorkflowRunResponse> runs) {
        this.runs = runs;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
