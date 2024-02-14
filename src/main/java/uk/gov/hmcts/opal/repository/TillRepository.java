package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.TillEntity;

@Repository
public interface TillRepository extends JpaRepository<TillEntity, Long>,
    JpaSpecificationExecutor<TillEntity> {
}
