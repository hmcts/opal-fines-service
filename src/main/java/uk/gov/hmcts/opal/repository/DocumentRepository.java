package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DocumentEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long>,
    JpaSpecificationExecutor<DocumentEntity> {

    Optional<DocumentEntity> findByDocumentId(String documentId);
}
