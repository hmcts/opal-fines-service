package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class PaymentTermsSearchDto implements ToJsonString {

    private String paymentTermsId;
    private String termsTypeCode;
    private String instalmentPeriod;
    private String jailDays;
    private String accountBalance;

}
