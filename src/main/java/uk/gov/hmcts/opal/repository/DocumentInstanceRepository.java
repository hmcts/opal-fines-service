package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;

@Repository
public interface DocumentInstanceRepository extends JpaRepository<DocumentInstanceEntity, Long> {

}
