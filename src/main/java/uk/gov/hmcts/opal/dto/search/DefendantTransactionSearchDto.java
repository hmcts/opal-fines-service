package uk.gov.hmcts.opal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DefendantTransactionSearchDto implements ToJsonString {

    private String defendantTransactionId;
    private String transactionType;
    private String paymentReference;
    private String text;
    private String writeOffCode;

}
