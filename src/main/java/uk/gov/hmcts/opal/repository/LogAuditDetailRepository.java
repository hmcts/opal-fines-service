package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;

@Repository
public interface LogAuditDetailRepository extends JpaRepository<LogAuditDetailEntity, Long>,
    JpaSpecificationExecutor<LogAuditDetailEntity> {

    @Procedure(name = "delete_expired_log_audit")
    void deleteExpiredLogAudit();
}
