package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.EnforcerLegacyEntity;

@Repository
public interface EnforcerRepository extends JpaRepository<EnforcerLegacyEntity, Long>,
    JpaSpecificationExecutor<EnforcerLegacyEntity> {

    EnforcerLegacyEntity findByEnforcerId(Long enforcerId);

    Optional<EnforcerLegacyEntity> findByEnforcerCodeAndBusinessUnit_businessUnitId(
        Short enforcerCode, Short businessUnitId);
}
