package uk.gov.hmcts.opal.service.proxy;

import org.mockito.Mock;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

import static org.mockito.Mockito.when;

public abstract class ProxyTestsBase {

    @Mock
    private DynamicConfigService dynamicConfigService;

    public void setLegacyMode(boolean legacyMode) {
        when(dynamicConfigService.isLegacyMode()).thenReturn(legacyMode);
    }
}
