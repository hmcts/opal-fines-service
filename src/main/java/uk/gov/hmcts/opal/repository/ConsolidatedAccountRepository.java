package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountEntity;

@Repository
public interface ConsolidatedAccountRepository extends JpaRepository<ConsolidatedAccountEntity, Long> {

    List<ConsolidatedAccountEntity> findByMasterAccountId(Long masterAccountId);
}
