package uk.gov.hmcts.opal.disco.proxy;

import org.mockito.Mock;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

import static org.mockito.Mockito.when;

public abstract class ProxyTestsBase {

    static final String LEGACY = "legacy";

    static final String OPAL = "opal";

    @Mock
    private DynamicConfigService dynamicConfigService;

    void setMode(String mode) {
        AppMode appMode = AppMode.builder().mode(mode).build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
    }
}
