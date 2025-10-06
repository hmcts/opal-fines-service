package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentTermsSummary {

    @XmlElement(name = "payment_terms_type")
    private LegacyPaymentTermsType paymentTermsType;

    @XmlElement(name = "effective_date")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate effectiveDate;

    @XmlElement(name = "instalment_period")
    private LegacyInstalmentPeriod instalmentPeriod;

    @XmlElement(name = "lump_sum_amount")
    private BigDecimal lumpSumAmount;

    @XmlElement(name = "instalment_amount")
    private BigDecimal instalmentAmount;

}
