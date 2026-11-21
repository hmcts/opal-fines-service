package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;

@Repository
public interface OpalProsecutorRepository extends JpaRepository<OpalProsecutorEntity, String>,
    JpaSpecificationExecutor<OpalProsecutorEntity> {
}
