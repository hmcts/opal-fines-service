package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyAddressDetails;
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
@XmlRootElement(name = "response")
public class LegacyGetDefendantAccountAtAGlanceResponse implements ToXmlString {

    @XmlElement(name = "version", required = true)
    private Long version;

    @XmlElement(name = "defendant_account_id", required = true)
    private String defendantAccountId;

    @XmlElement(name = "account_number", required = true)
    private String accountNumber;

    @XmlElement(name = "debtor_type", required = true)
    private String debtorType;

    @XmlElement(name = "is_youth", required = true)
    private boolean youth;

    @XmlElement(name = "party_details", required = true)
    private LegacyPartyDetails partyDetails;

    @XmlElement(name = "address", required = true)
    private LegacyAddressDetails address;

    @XmlElement(name = "language_preferences")
    private LanguagePreferences languagePreferences; // optional

    @XmlElement(name = "payment_terms_summary", required = true)
    private PaymentTermsSummary paymentTermsSummary;

    @XmlElement(name = "enforcement_status_summary", required = true)
    private EnforcementStatusSummary enforcementStatusSummary;

    @XmlElement(name = "comments_and_notes")
    private CommentsAndNotes commentsAndNotes; // optional

}
