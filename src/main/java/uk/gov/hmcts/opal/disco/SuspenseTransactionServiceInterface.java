package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;

import java.util.List;

public interface SuspenseTransactionServiceInterface {

    SuspenseTransactionEntity getSuspenseTransaction(long suspenseTransactionId);

    List<SuspenseTransactionEntity> searchSuspenseTransactions(SuspenseTransactionSearchDto criteria);
}
