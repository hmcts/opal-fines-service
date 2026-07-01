package uk.gov.hmcts.opal.dto.history;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryCreditorTransactionDetails implements AccountHistoryDetails {

    private String transactionType;

    private String paymentReference;

    private String status;

    private LocalDateTime statusDate;

    private String associatedRecordType;

    private String associatedRecordId;

    private String accountNumber;

    private String defendantAccountNumber;

    private Long defendantAccountId;
}
