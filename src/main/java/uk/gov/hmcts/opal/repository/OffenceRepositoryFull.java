package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.offence.OffenceFullEntity;

@Repository
public interface OffenceRepositoryFull extends JpaRepository<OffenceFullEntity, Long>,
    JpaSpecificationExecutor<OffenceFullEntity> {
}
