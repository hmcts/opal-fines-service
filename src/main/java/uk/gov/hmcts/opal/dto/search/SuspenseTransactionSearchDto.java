package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class SuspenseTransactionSearchDto implements ToJsonString {

    private String suspenseTransactionId;
    private String suspenseItemId;
    private String postedBy;
    private String postedByUserId;
    private String transactionType;
    private String associatedRecordType;
    private String associatedRecordId;
    private String text;
    private String reversed;

}
