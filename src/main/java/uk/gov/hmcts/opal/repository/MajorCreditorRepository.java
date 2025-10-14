package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorFullEntity;

@Repository
public interface MajorCreditorRepository extends JpaRepository<MajorCreditorFullEntity, Long>,
    JpaSpecificationExecutor<MajorCreditorFullEntity> {
}
