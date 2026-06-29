package uk.gov.hmcts.opal.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MinorCreditorTransactionHistoryProjection {

    Long getCreditorTransactionId();

    LocalDateTime getPostedDate();

    String getPostedBy();

    String getPostedByName();

    String getTransactionType();

    BigDecimal getTransactionAmount();

    String getPaymentReference();

    String getStatus();

    LocalDateTime getStatusDate();

    String getAssociatedRecordType();

    String getAssociatedRecordId();

    String getAccountNumber();

    String getDefendantAccountNumber();

    Long getDefendantAccountId();
}
