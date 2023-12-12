package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.EnforcersEntity;

@Repository
public interface EnforcersRepository extends JpaRepository<EnforcersEntity, Long> {

    EnforcersEntity findByEnforcerId(Long enforcerId);
}
