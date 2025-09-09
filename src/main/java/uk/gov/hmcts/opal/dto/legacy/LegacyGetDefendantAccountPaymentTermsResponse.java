package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

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
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate paymentCardLastRequested;

    @XmlElement(name = "date_last_amended")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateLastAmended;

    @XmlElement(name = "extension")
    private Boolean extension;

    @XmlElement(name = "last_enforcement")
    private String lastEnforcement;
}
