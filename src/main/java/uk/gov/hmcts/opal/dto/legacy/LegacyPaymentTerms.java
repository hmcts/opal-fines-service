package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement(name = "payment_terms")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyPaymentTerms {

    @XmlElement(name = "days_in_default")
    private Integer daysInDefault;

    @XmlElement(name = "date_days_default_imposed")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateDaysInDefaultImposed;

    @XmlElement(name = "extension")
    private boolean extension;

    @XmlElement(name = "reason_for_extension")
    private String reasonForExtension;

    @XmlElement(name = "payment_terms_type")
    private LegacyPaymentTermsType paymentTermsType;

    @XmlElement(name = "effective_date")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate effectiveDate;

    @XmlElement(name = "installment_period")
    private LegacyInstalmentPeriod instalmentPeriod;

    @XmlElement(name = "lump_sum_amount")
    private BigDecimal lumpSumAmount;

    @XmlElement(name = "installment_amount")
    private BigDecimal instalmentAmount;

    @XmlElement(name = "posted_details")
    private LegacyPostedDetails postedDetails;
}
