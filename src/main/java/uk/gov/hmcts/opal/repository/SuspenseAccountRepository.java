package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;

@Repository
public interface SuspenseAccountRepository extends JpaRepository<SuspenseAccountEntity, Long>,
    JpaSpecificationExecutor<SuspenseAccountEntity> {
}
