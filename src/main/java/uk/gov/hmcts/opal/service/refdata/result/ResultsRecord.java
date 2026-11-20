package uk.gov.hmcts.opal.service.refdata.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultsRecord {
    @JsonProperty("result_code")
    private String resultCode;

    @JsonProperty("result_title")
    private String resultTitle;

    @JsonProperty("result_title_welsh")
    private String resultTitleWelsh;

    @JsonProperty("result_type")
    private String resultType;

    @JsonProperty("imposition")
    private Boolean imposition;

    @JsonProperty("imposition_category")
    private String impositionCategory;

    @JsonProperty("imposition_allocation_priority")
    private BigInteger impositionAllocationPriority;

    @JsonProperty("imposition_accruing")
    private Boolean impositionAccruing;

    @JsonProperty("imposition_creditor")
    private String impositionCreditor;

    @JsonProperty("enforcement")
    private Boolean enforcement;

    @JsonProperty("enforcement_override")
    private Boolean enforcementOverride;

    @JsonProperty("further_enforcement_warning_required")
    private Boolean furtherEnforcementWarningRequired;

    @JsonProperty("further_enforcement_disallowed")
    private Boolean furtherEnforcementDisallowed;

    @JsonProperty("enforcement_on_hold")
    private Boolean enforcementOnHold;

    @JsonProperty("requires_enforcer")
    private Boolean requiresEnforcer;

    @JsonProperty("generates_a_hearing")
    private Boolean generatesAHearing;

    @JsonProperty("generates_a_warrant")
    private Boolean generatesAWarrant;

    @JsonProperty("collection_order")
    private Boolean collectionOrder;

    @JsonProperty("disallow_ttp_extension")
    private Boolean disallowTtpExtension;

    @JsonProperty("preserve_last_enforcement_on_ttp_extension")
    private Boolean preserveLastEnforcementOnTtpExtension;

    @JsonProperty("prevent_payment_card")
    private Boolean preventPaymentCard;

    @JsonProperty("lists_monies")
    private Boolean listsMonies;

    @JsonProperty("allow_payment_terms")
    private Boolean allowPaymentTerms;

    @JsonProperty("requires_employment_data")
    private Boolean requiresEmploymentData;

    @JsonProperty("allow_additional_action")
    private Boolean allowAdditionalAction;

    @JsonProperty("next_permitted_actions_type")
    private String nextPermittedActionsType;

    @JsonProperty("next_permitted_action_count")
    private BigInteger nextPermittedActionCount;

    @JsonProperty("requires_lja")
    private Boolean requiresLja;

    @JsonProperty("manual_enforcement")
    private Boolean manualEnforcement;

    @JsonProperty("auto_enforcement")
    private Boolean autoEnforcement;

    @JsonProperty("publishing_status")
    private String publishingStatus;

    @JsonProperty("result_parameters")
    private JsonNode resultParameters;

    @JsonProperty("result_next_permitted_action_mappings")
    private JsonNode resultNextPermittedActionMappings;

}
