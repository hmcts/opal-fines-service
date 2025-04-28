package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

@Repository
public interface BusinessUnitLiteRepository extends JpaRepository<BusinessUnit.Lite, Short>,
    JpaSpecificationExecutor<BusinessUnit.Lite> {
}
