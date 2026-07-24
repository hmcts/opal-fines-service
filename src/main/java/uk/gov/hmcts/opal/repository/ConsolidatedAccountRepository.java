package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountEntityId;

@Repository
public interface ConsolidatedAccountRepository extends JpaRepository<ConsolidatedAccountEntity,
    ConsolidatedAccountEntityId> {

    List<ConsolidatedAccountEntity> findByMasterAccountIdOrderByChildAccountIdAsc(Long masterAccountId);
}
