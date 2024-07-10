package uk.gov.hmcts.opal.repository.print;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.print.PrintDefinition;


@Repository
public interface PrintDefinitionRepository extends JpaRepository<PrintDefinition, Long> {
    @Transactional
    PrintDefinition findByDocTypeAndTemplateId(String docType, String templateId);
}
