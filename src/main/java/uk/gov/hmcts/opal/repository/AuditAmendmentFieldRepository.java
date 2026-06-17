package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.auditamendmentfield.AuditAmendmentFieldEntity;

@Repository
public interface AuditAmendmentFieldRepository extends JpaRepository<AuditAmendmentFieldEntity, Short> {
}
