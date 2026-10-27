package uk.gov.hmcts.opal.service.reference.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

@Service
@RequiredArgsConstructor
public class DraftAccountReferenceValidationServiceLocator implements ProxyInterface {
    private final DynamicConfigService dynamicConfigService;
    private final OpalDraftAccountReferenceValidationService opalService;
    private final LegacyDraftAccountReferenceValidationService legacyService;

    public DraftAccountReferenceValidationService getService() {
        return isLegacyMode(dynamicConfigService) ? legacyService : opalService;
    }
}
