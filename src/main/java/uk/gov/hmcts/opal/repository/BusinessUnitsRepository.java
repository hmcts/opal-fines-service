package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.BusinessUnitsEntity;

@Repository
public interface BusinessUnitsRepository extends JpaRepository<BusinessUnitsEntity, Long> {
}
