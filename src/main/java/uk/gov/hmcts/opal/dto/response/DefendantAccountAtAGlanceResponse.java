package uk.gov.hmcts.opal.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentTermsSummary;
import uk.gov.hmcts.opal.util.Versioned;

/**
 * Defendant Account At A Glance Response.
 *
 * <p>
 * based on getDefendantAccountAtAGlanceResponse.json
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DefendantAccountAtAGlanceResponse implements ToJsonString, Versioned {

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("debtor_type")
    private String debtorType;

    @JsonProperty("is_youth")
    private Boolean isYouth;

    @JsonProperty("party_details")
    private PartyDetails partyDetails;

    @JsonProperty("address")
    private AddressDetails addressDetails;

    @JsonProperty("language_preferences")
    private LanguagePreferences languagePreferences;

    @JsonProperty("payment_terms")
    private PaymentTermsSummary paymentTermsSummary;

    @JsonProperty("enforcement_status")
    private EnforcementStatusSummary enforcementStatus;

    @JsonProperty("comments_and_notes")
    private CommentsAndNotes commentsAndNotes;

    @JsonIgnore
    private BigInteger version;
}
