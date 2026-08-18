package uk.gov.hmcts.opal.dto.history;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryPaymentTermsDetails implements AccountHistoryDetails {

    private Integer daysInDefault;

    private LocalDate dateDaysInDefaultImposed;

    private String reasonForExtension;

    private PaymentTermsType paymentTermsType;

    private LocalDate effectiveDate;

    private InstalmentPeriod instalmentPeriod;

    private BigDecimal lumpSumAmount;

    private BigDecimal instalmentAmount;
}
