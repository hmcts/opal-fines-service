package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Data
public class DefendantAccountDto {

    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("amount_imposed")
    private BigDecimal amountImposed;

    @JsonProperty("amount_paid")
    private BigDecimal amountPaid;

    @JsonProperty("account_balance")
    private BigDecimal accountBalance;

    @JsonProperty("business_unit_id")
    private Integer businessUnitId;

    @JsonProperty("business_unit_name")
    private String businessUnitName;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("originator_name")
    private String originatorName;

    @JsonProperty("imposed_hearing_date")
    private LocalDate imposedHearingDate;

    @JsonProperty("imposing_court_code")
    private Integer imposingCourtCode;

    @JsonProperty("last_hearing_date")
    private String lastHearingDate;

    @JsonProperty("last_hearing_court_code")
    private Integer lastHearingCourtCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("last_changed_date")
    private LocalDate lastChangedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("last_movement_date")
    private LocalDate lastMovementDate;

    @JsonProperty("collection_order")
    private Boolean collectionOrder;

    @JsonProperty("enforcing_court_code")
    private Integer enforcingCourtCode;

    @JsonProperty("last_enforcement")
    private String lastEnforcement;

    @JsonProperty("enf_override_result_id")
    private String enfOverrideResultId;

    @JsonProperty("enf_override_enforcer_code")
    private Short enfOverrideEnforcerCode;

    @JsonProperty("enf_override_tfo_lja_code")
    private Integer enfOverrideTfoLjaCode;

    @JsonProperty("prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonProperty("account_comments")
    private String accountComments;

    @JsonProperty("payment_terms")
    private PaymentTermsDto paymentTerms;

    @JsonProperty("parties")
    private List<PartyDto> parties;

    @JsonProperty("impositions")
    private List<ImpositionDto> impositions;

    @JsonProperty("account_activities")
    private List<AccountActivityDto> accountActivities;

}
