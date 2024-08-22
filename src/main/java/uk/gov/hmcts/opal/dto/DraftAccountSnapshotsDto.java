package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftAccountSnapshotsDto implements ToJsonString {

    @JsonProperty("AccountSnapshot")
    private List<Snapshot> accountSnapshot;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Snapshot implements ToJsonString {
        @JsonProperty("DefendantName")
        private String defendantName;

        @JsonProperty("DateOfBirth")
        private String dateOfBirth;

        @JsonProperty("CreatedDate")
        private OffsetDateTime createdDate;

        @JsonProperty("AccountType")
        private String accountType;

        @JsonProperty("SubmittedBy")
        private String submittedBy;

        @JsonProperty("ApprovedDate")
        private OffsetDateTime approvedDate;

        @JsonProperty("BusinessUnitName")
        private String businessUnitName;
    }
}
