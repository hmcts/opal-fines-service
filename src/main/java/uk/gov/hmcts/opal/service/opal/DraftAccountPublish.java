package uk.gov.hmcts.opal.service.opal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.service.DraftAccountPublishInterface;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactions;

@Service
@Slf4j(topic = "opal.DraftAccountPublish")
@RequiredArgsConstructor
public class DraftAccountPublish implements DraftAccountPublishInterface {

    private final DraftAccountTransactions draftAccountTransactions;

    @Override
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity publishEntity, BusinessUnitUser unitUser) {
        log.debug(":promoteToDefendantAccount: About to call Out to Opal PostgreSQL Stored Procedure");

        // TODO - Uncomment this when Stored Proc completed in DB
        // Map<String, Object> outputs = draftAccountTransactions.publishAccountStoredProc(publishEntity);
        //
        // String accountNumber = outputs.getOrDefault("pio_account_number", "<null>").toString();
        // Long accountId = Long.parseLong(outputs.getOrDefault("pio_defendant_account_id", "0").toString());
        // log.info(":publishDefendantAccount: \n\nPublished,  account number: {}, account id: {}\n\n",
        //          accountNumber, accountId);
        //
        // publishEntity.setAccountId(accountId);
        // publishEntity.setAccountNumber(accountNumber);
        return draftAccountTransactions.updateStatus(publishEntity, DraftAccountStatus.PUBLISHED,
                                                     draftAccountTransactions);
    }
}
