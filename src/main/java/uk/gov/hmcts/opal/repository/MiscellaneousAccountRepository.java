package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;

@Repository
public interface MiscellaneousAccountRepository extends JpaRepository<MiscellaneousAccountEntity, Long> {

    List<MiscellaneousAccountEntity> findAllByMiscellaneousAccountIdIn(List<Long> miscellaneousAccountIds);
}
