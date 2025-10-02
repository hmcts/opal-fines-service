package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.AddressDetails;
import uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.PaymentTermsSummary;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyGetDefendantAccountAtAGlanceResponse implements ToXmlString {

    @XmlElement(name = "version")
    private Long version;

    @XmlElement(name = "defendant_account_id")
    private String defendantAccountId;

    @XmlElement(name = "account_number")
    private String accountNumber;

    @XmlElement(name = "debtor_type")
    private String debtorType;

    @XmlElement(name = "is_youth")
    private Boolean isYouth;

    @XmlElement(name = "party_details")
    private LegacyPartyDetails partyDetails;

    @XmlElement(name = "address")
    private AddressDetails address;

    @XmlElement(name = "language_preferences")
    private LanguagePreferences languagePreferences;

    @XmlElement(name = "payment_terms")
    private PaymentTermsSummary paymentTerms;

    @XmlElement(name = "enforcement_status")
    private EnforcementStatusSummary enforcementStatus;

    @XmlElement(name = "comments_and_notes")
    private CommentsAndNotes commentsAndNotes;
}
