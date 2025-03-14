package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;

@Repository
public interface HmrcRequestRepository extends JpaRepository<HmrcRequestEntity, Long>,
    JpaSpecificationExecutor<HmrcRequestEntity> {
}
