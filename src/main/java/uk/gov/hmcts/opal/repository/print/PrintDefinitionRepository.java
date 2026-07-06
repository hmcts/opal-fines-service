package uk.gov.hmcts.opal.repository.print;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.print.PrintDefinitionEntity;


@Repository
public interface PrintDefinitionRepository extends JpaRepository<PrintDefinitionEntity, Long> {
    @Transactional
    PrintDefinitionEntity findByDocTypeAndTemplateId(String docType, String templateId);
}
