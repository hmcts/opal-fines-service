package uk.gov.hmcts.opal.documents;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    @Query("""
        SELECT dt FROM DocumentTemplate dt
        WHERE dt.documentType = :documentType
        AND current_timestamp >= dt.activeFrom
        AND (dt.activeTo is null or current_timestamp < dt.activeTo)
        """)
    List<DocumentTemplate> findActiveDocumentTemplateFromType(DocumentTemplate.DocumentType documentType);
}
