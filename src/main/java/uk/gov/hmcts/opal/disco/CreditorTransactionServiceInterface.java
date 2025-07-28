package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;

import java.util.List;

public interface CreditorTransactionServiceInterface {

    CreditorTransactionEntity getCreditorTransaction(long creditorTransactionId);

    List<CreditorTransactionEntity> searchCreditorTransactions(CreditorTransactionSearchDto criteria);
}
