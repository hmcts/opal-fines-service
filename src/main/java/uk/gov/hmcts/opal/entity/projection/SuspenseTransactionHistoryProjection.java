package uk.gov.hmcts.opal.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SuspenseTransactionHistoryProjection {

    Long getTransactionId();

    String getTransactionType();

    BigDecimal getAmount();

    LocalDateTime getPostedDate();

    String getPostedBy();

    String getPostedByName();

    String getAssociatedRecordType();

    String getAssociatedRecordId();

    String getText();

    String getReversed();

}
