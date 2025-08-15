package uk.gov.hmcts.opal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.PaymentTermsSummary;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DefendantAccountAtAGlanceResponse implements ToJsonString {

    //FIXME - based on getDefendantAccountAtAGlanceResponse.json

    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("debtor_type")
    private String debtorType;

    @JsonProperty("is_youth")
    private Boolean isYouth;

    @JsonProperty("party_details")
    private PartyDto partyDetails;

    @JsonProperty("address_details")
    private AddressDetails addressDetails;

    @JsonProperty("language_preferences")
    private LanguagePreferences languagePreferences;

    @JsonProperty("payment_terms")
    private PaymentTermsSummary paymentTermsSummary;

    @JsonProperty("enforcement_status")
    private EnforcementStatusSummary enforcementStatus;

    @JsonProperty("comments_and_notes")
    private CommentsAndNotes commentsAndNotes;
}
