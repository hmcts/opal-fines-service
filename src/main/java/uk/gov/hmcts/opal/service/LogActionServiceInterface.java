package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;

import java.util.List;

public interface LogActionServiceInterface {

    LogActionEntity getLogAction(short logActionId);

    List<LogActionEntity> searchLogActions(LogActionSearchDto criteria);
}
