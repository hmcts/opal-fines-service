package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;

import java.util.List;

public interface DefendantTransactionServiceInterface {

    DefendantTransactionEntity getDefendantTransaction(long defendantTransactionId);

    List<DefendantTransactionEntity> searchDefendantTransactions(DefendantTransactionSearchDto criteria);
}
