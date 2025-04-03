package uk.gov.hmcts.opal.zephyr.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ExecutionUpdateVO extends BaseJiraVO {

    @JsonProperty("status")
    private String status;

    @JsonProperty("comment")
    private String comment;

    // optional
    @JsonProperty("defectList")
    private List<String> defectList;

    // optional - set to false if no need of defect update
    @JsonProperty("updateDefectList")
    private String updateDefectList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getDefectList() {
        return defectList;
    }

    public void setDefectList(List<String> defectList) {
        this.defectList = defectList;
    }

    public String getUpdateDefectList() {
        return updateDefectList;
    }

    public void setUpdateDefectList(String updateDefectList) {
        this.updateDefectList = updateDefectList;
    }
}
