package uk.gov.hmcts.opal.service.proxy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;
import uk.gov.hmcts.opal.service.iface.LocalJusticeAreaServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyLocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalLocalJusticeAreaService;

@Service
@RequiredArgsConstructor
public class LocalJusticeAreaServiceProxy implements LocalJusticeAreaServiceInterface, ProxyInterface {

    private final OpalLocalJusticeAreaService opalLocalJusticeAreaService;
    private final LegacyLocalJusticeAreaService legacyLocalJusticeAreaService;
    private final DynamicConfigService dynamicConfigService;

    private LocalJusticeAreaServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyLocalJusticeAreaService : opalLocalJusticeAreaService;
    }

    @Override
    public LocalJusticeArea getLocalJusticeAreaById(Short localJusticeAreaId) {
        return getCurrentModeService().getLocalJusticeAreaById(localJusticeAreaId);
    }

    @Override
    public LocalJusticeAreasResponse getLocalJusticeAreaRefData(
        @Nullable String q, @Nullable List<LocalJusticeAreaType> ljaTypes) {

        return getCurrentModeService().getLocalJusticeAreaRefData(q, ljaTypes);
    }
}
