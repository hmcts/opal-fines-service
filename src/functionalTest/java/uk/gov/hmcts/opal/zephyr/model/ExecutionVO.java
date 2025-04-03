package uk.gov.hmcts.opal.zephyr.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExecutionVO extends BaseJiraVO {

    @JsonProperty("cycleId")
    private String cycleId;

    @JsonProperty("issueId")
    private String issueId;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("versionId")
    private String versionId;

    @JsonProperty("assigneeType")
    private String assigneeType;

    @JsonProperty("assignee")
    private String assignee;

    @JsonProperty("folderId")
    private float folderId;

    public String getCycleId() {
        return cycleId;
    }

    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getAssigneeType() {
        return assigneeType;
    }

    public void setAssigneeType(String assigneeType) {
        this.assigneeType = assigneeType;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public float getFolderId() {
        return folderId;
    }

    public void setFolderId(float folderId) {
        this.folderId = folderId;
    }
}
