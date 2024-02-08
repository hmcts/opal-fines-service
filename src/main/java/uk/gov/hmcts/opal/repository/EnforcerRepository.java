package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.EnforcerEntity;

@Repository
public interface EnforcerRepository extends JpaRepository<EnforcerEntity, Long>,
    JpaSpecificationExecutor<EnforcerEntity> {

    EnforcerEntity findByEnforcerId(Long enforcerId);
}
