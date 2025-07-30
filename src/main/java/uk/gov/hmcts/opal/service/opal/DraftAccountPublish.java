package uk.gov.hmcts.opal.service.opal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.TimelineData;
import uk.gov.hmcts.opal.service.iface.DraftAccountPublishInterface;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactions;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_ID;
import static uk.gov.hmcts.opal.entity.draft.StoredProcedureNames.DEF_ACC_NO;

@Service
@Slf4j(topic = "opal.DraftAccountPublish")
@RequiredArgsConstructor
public class DraftAccountPublish implements DraftAccountPublishInterface {

    private final DraftAccountTransactions draftAccountTransactions;

    @Override
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity publishEntity, BusinessUnitUser unitUser) {
        log.debug(":publishDefendantAccount: About to call Out to Opal PostgreSQL Stored Procedure");

        try {
            Map<String, Object> outputs = draftAccountTransactions.publishAccountStoredProc(publishEntity);

            String accountNumber = outputs.getOrDefault(DEF_ACC_NO, "<null>").toString();
            Long accountId = Long.parseLong(outputs.getOrDefault(DEF_ACC_ID, "0").toString());
            log.debug(":publishDefendantAccount: \n\nPublished,  account number: {}, account id: {}\n\n",
                     accountNumber, accountId);

            publishEntity.setAccountId(accountId);
            publishEntity.setAccountNumber(accountNumber);
            return draftAccountTransactions.updateStatus(publishEntity, DraftAccountStatus.PUBLISHED,
                                                         draftAccountTransactions);
        } catch (JpaSystemException | InvalidDataAccessApiUsageException e) {
            log.error(":publishDefendantAccount: Error Class: {}", e.getClass().getName());
            log.error(":publishDefendantAccount: ", e);
            TimelineData timelineData = new TimelineData(publishEntity.getTimelineData());
            timelineData.insertEntry(
                unitUser.getBusinessUnitUserId(), DraftAccountStatus.PUBLISHING_FAILED.getLabel(),
                LocalDate.now(), e.getMessage()
            );
            publishEntity.setTimelineData(timelineData.toJson());
            publishEntity.setStatusMessage(e.getMessage());
            return draftAccountTransactions.updateStatus(publishEntity, DraftAccountStatus.PUBLISHING_FAILED,
                                                         draftAccountTransactions);
        }
    }
}
