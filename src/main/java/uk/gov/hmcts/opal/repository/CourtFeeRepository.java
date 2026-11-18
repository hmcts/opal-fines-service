package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

@Repository
public interface CourtFeeRepository extends JpaRepository<CourtFeeEntity, Long> {

    List<CourtFeeEntity> findAllByBusinessUnit(BusinessUnitEntity businessUnit);
}
