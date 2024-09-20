package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftAccountSnapshots implements ToJsonString {

    @JsonProperty("account_snapshot")
    private List<Snapshot> accountSnapshot;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Snapshot implements ToJsonString {
        @JsonProperty("defendant_name")
        private String defendantName;

        @JsonProperty("date_of_birth")
        private String dateOfBirth;

        @JsonProperty("created_date")
        private OffsetDateTime createdDate;

        @JsonProperty("account_type")
        private String accountType;

        @JsonProperty("submitted_by")
        private String submittedBy;

        @JsonProperty("approved_date")
        private OffsetDateTime approvedDate;

        @JsonProperty("business_unit_name")
        private String businessUnitName;
    }
}
