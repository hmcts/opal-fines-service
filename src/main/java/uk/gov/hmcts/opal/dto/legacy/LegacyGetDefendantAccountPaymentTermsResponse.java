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

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyGetDefendantAccountPaymentTermsResponse implements ToXmlString {

    private Integer version;

    @XmlElement(name = "payment_terms")
    private LegacyPaymentTerms paymentTerms;

    @XmlElement(name = "posted_details")
    private LegacyPostedDetails postedDetails;

    @XmlElement(name = "payment_card_last_requested")
    private LocalDate paymentCardLastRequested;

    @XmlElement(name = "date_last_amended")
    private LocalDate dateLastAmended;

    @XmlElement(name = "extension")
    private Boolean extension;

    @XmlElement(name = "last_enforcement")
    private String lastEnforcement;
}
