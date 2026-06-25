package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.service.iface.DraftAccountPublishInterface;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactional;

@Service
@Slf4j(topic = "opal.DraftAccountPublish")
@RequiredArgsConstructor
public class DraftAccountPublish implements DraftAccountPublishInterface {

    private final DraftAccountTransactional draftAccountTransactional;
    private final Clock clock;

    @Override
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity publishEntity, BusinessUnitUser unitUser) {
        log.debug(":publishDefendantAccount: entity {}", publishEntity);
        log.debug(":publishDefendantAccount: About to call Out to Opal PostgreSQL Stored Procedure");

        try {
            return draftAccountTransactional.publishDefendantAccount(publishEntity);
        } catch (JpaSystemException | InvalidDataAccessApiUsageException e) {
            log.error(":publishDefendantAccount: Error Class: {}", e.getClass().getName());
            log.error(":publishDefendantAccount: ", e);

            TimelineData timelineData = new TimelineData(publishEntity.getTimelineData());
            String operationId = LogUtil.getOrCreateOpalOperationId();
            String reason = "%s Error code: [%s]".formatted(LogUtil.ERRMSG_STORED_PROC_FAILURE, operationId);
            timelineData.insertEntry(
                unitUser.getBusinessUnitUserId(), DraftAccountStatus.PUBLISHING_FAILED.getLabel(),
                LocalDate.now(clock), reason
            );

            DraftAccountEntity failedUpdate = new DraftAccountEntity();
            failedUpdate.setDraftAccountId(publishEntity.getDraftAccountId());
            failedUpdate.setTimelineData(timelineData.toJson());
            failedUpdate.setStatusMessage(LogUtil.ERRMSG_STORED_PROC_FAILURE);
            failedUpdate.setVersionNumber(publishEntity.getVersionNumber());

            draftAccountTransactional.updateStatus(
                failedUpdate,
                DraftAccountStatus.PUBLISHING_FAILED,
                draftAccountTransactional
            );
            throw e;
        }
    }
}
