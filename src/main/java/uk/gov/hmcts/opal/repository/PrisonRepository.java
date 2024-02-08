package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.PrisonEntity;

@Repository
public interface PrisonRepository extends JpaRepository<PrisonEntity, Long>,
    JpaSpecificationExecutor<PrisonEntity> {

    PrisonEntity findByPrisonId(Long prisonId);
}
