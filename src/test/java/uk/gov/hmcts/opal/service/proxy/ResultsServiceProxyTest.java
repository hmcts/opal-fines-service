package uk.gov.hmcts.opal.service.proxy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalResultService;
import uk.gov.hmcts.opal.service.opal.ResultService;

@ExtendWith(MockitoExtension.class)
class ResultsServiceProxyTest {
    @Mock private ResultService legacyResultService;
    @Mock private OpalResultService opalResultService;
    @Mock private DynamicConfigService dynamicConfigService;
    @InjectMocks private ResultsServiceProxy proxy;

    @Test
    void routesLegacyRequestsToLegacyService() {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);
        proxy.getResult("A", true);
        proxy.getResultsByIds(List.of("A"), true, false, true, false, true);
        verify(legacyResultService).getResult("A", true);
        verify(legacyResultService).getResultsByIds(List.of("A"), true, false, true, false, true);
    }

    @Test
    void routesOpalRequestsToOpalService() {
        when(dynamicConfigService.isLegacyMode()).thenReturn(false);
        proxy.getResult("A", true);
        proxy.getResultsByIds(List.of("A"), true, false, true, false, true);
        verify(opalResultService).getResult("A", true);
        verify(opalResultService).getResultsByIds(List.of("A"), true, false, true, false, true);
    }
}
