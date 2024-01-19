package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName(value = "imposition")
class ImpositionDto {
    @JsonProperty("imposition_id")
    private Integer impositionId;

    @JsonProperty("posted_date")
    private String postedDate;

    @JsonProperty("result_id")
    private String resultId;

    @JsonProperty("imposed_date")
    private String imposedDate;

    @JsonProperty("imposing_court_code")
    private Integer imposingCourtCode;

    @JsonProperty("imposed_amount")
    private Double imposedAmount;

    @JsonProperty("paid_amount")
    private Double paidAmount;

    @JsonProperty("offence_title")
    private String offenceTitle;

    @JsonProperty("creditor_account_number")
    private String creditorAccountNumber;

    @JsonProperty("creditor_name")
    private String creditorName;

}
