package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class DefendantTransactionSearchDto implements ToJsonString {

    private String defendantTransactionId;
    private String transactionType;
    private String paymentReference;
    private String text;
    private String writeOffCode;

}
