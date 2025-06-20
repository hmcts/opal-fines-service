package uk.gov.hmcts.opal.service.opal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.service.DraftAccountPublishInterface;

@Service
@Slf4j(topic = "opal.DraftAccountPublish")
@RequiredArgsConstructor
public class DraftAccountPublish implements DraftAccountPublishInterface {

    @Override
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity updatedEntity, BusinessUnitUser unitUser) {
        // TODO - Call to Stored Procedure in Opal Repository
        log.info(":promoteToDefendantAccount: TODO - Call Out to Stored Procedure");
        return updatedEntity;
    }
}
