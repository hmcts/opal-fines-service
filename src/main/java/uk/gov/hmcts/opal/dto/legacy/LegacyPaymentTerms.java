package uk.gov.hmcts.opal.dto.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyPaymentTerms {

    private Integer daysInDefault;

    private LocalDate dateDaysInDefaultImposed;

    private String reasonForExtension;

    private LegacyPaymentTermsType paymentTermsType;

    private LocalDate effectiveDate;

    private LegacyInstalmentPeriod instalmentPeriod;

    private BigDecimal lumpSumAmount;

    private BigDecimal instalmentAmount;
}
