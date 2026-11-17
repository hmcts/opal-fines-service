package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;
import uk.gov.hmcts.opal.service.iface.ProsecutorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyProsecutorService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalProsecutorService;

@Service
@RequiredArgsConstructor
public class ProsecutorServiceProxy implements ProsecutorServiceInterface, ProxyInterface {

    private final OpalProsecutorService opalProsecutorService;
    private final LegacyProsecutorService legacyProsecutorService;
    private final DynamicConfigService dynamicConfigService;

    private ProsecutorServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyProsecutorService : opalProsecutorService;
    }

    @Override
    public ProsecutorsObject getProsecutorByKey(Long prosecutorKey) {
        return getCurrentModeService().getProsecutorByKey(prosecutorKey);
    }

    @Override
    public ProsecutorsReferenceDataResponse getProsecutorsRefData(@Nullable String q) {
        return getCurrentModeService().getProsecutorsRefData(q);
    }
}
