package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;

import java.util.List;

public interface LogAuditDetailServiceInterface {

    LogAuditDetailEntity getLogAuditDetail(long logAuditDetailId);

    List<LogAuditDetailEntity> searchLogAuditDetails(LogAuditDetailSearchDto criteria);
}
