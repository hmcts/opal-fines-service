package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultDto {

    @JsonProperty("result_id")
    private String resultId;

    @JsonProperty("result_title")
    private String resultTitle;

    @JsonProperty("result_title_cy")
    private String resultTitleCy;

    @JsonProperty("result_type")
    private String resultType;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("imposition_allocation_priority")
    private Short impositionAllocationPriority;

    @JsonProperty("imposition_creditor")
    private String impositionCreditor;

    @JsonProperty("imposition")
    private boolean imposition;

    @JsonProperty("imposition_category")
    private String impositionCategory;

    @JsonProperty("imposition_accruing")
    private Boolean impositionAccruing;

    @JsonProperty("enforcement")
    private boolean enforcement;

    @JsonProperty("enforcement_override")
    private boolean enforcementOverride;

    @JsonProperty("further_enforcement_warn")
    private boolean furtherEnforcementWarn;

    @JsonProperty("further_enforcement_disallow")
    private boolean furtherEnforcementDisallow;

    @JsonProperty("enforcement_hold")
    private boolean enforcementHold;

    @JsonProperty("requires_enforcer")
    private boolean requiresEnforcer;

    @JsonProperty("generates_hearing")
    private boolean generatesHearing;

    @JsonProperty("collection_order")
    private boolean collectionOrder;

    @JsonProperty("extend_ttp_disallow")
    private boolean extendTtpDisallow;

    @JsonProperty("extend_ttp_preserve_last_enf")
    private boolean extendTtpPreserveLastEnf;

    @JsonProperty("prevent_payment_card")
    private boolean preventPaymentCard;

    @JsonProperty("lists_monies")
    private boolean listsMonies;

    @JsonProperty("result_parameters")
    private String resultParameters;
}
