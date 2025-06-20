package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.service.DraftAccountPublishInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyDraftAccountPublish;
import uk.gov.hmcts.opal.service.opal.DraftAccountPublish;

@Service
@Slf4j(topic = "opal.DraftAccountPublishProxy")
@RequiredArgsConstructor
public class DraftAccountPublishProxy implements DraftAccountPublishInterface, ProxyInterface {

    private final DraftAccountPublish draftAccountPromotion;
    private final LegacyDraftAccountPublish legacyDraftAccountPromotion;
    private final DynamicConfigService dynamicConfigService;

    private DraftAccountPublishInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDraftAccountPromotion : draftAccountPromotion;
    }

    @Override
    public DraftAccountEntity publishDefendantAccount(DraftAccountEntity updatedEntity, BusinessUnitUser unitUser) {
        return getCurrentModeService().publishDefendantAccount(updatedEntity, unitUser);
    }
}
