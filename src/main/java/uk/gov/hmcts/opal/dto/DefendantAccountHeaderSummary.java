package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountHeaderSummary implements ToJsonString {

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("has_parent_guardian")
    private Boolean hasParentGuardian;

    @JsonProperty("debtor_type")
    private String debtorType;

    @JsonProperty("organisation")
    private Boolean organisation;

    @JsonProperty("account_status_display_name")
    private String accountStatusDisplayName;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonProperty("fixed_penalty_ticket_number")
    private String fixedPenaltyTicketNumber;

    @JsonProperty("business_unit_name")
    private String businessUnitName;

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("business_unit_code")
    private String businessUnitCode;

    @JsonProperty("imposed")
    private BigDecimal imposed;

    @JsonProperty("arrears")
    private BigDecimal arrears;

    @JsonProperty("paid")
    private BigDecimal paid;

    @JsonProperty("written_off")
    private BigDecimal writtenOff;

    @JsonProperty("account_balance")
    private BigDecimal accountBalance;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("is_youth")
    private Boolean isYouth;

    @JsonProperty("title")
    private String title;

    @JsonProperty("firstnames")
    private String firstnames;

    @JsonProperty("surname")
    private String surname;
}
