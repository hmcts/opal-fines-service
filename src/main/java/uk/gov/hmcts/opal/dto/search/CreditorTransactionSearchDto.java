package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class CreditorTransactionSearchDto implements ToJsonString {

    private String creditorTransactionId;
    private String creditorAccountId;
    private String postedBy;
    private String postedByUserId;
    private String transactionType;
    private String impositionResultId;
    private String paymentReference;
    private String status;
    private String associatedRecordType;
    private String associatedRecordId;

}
