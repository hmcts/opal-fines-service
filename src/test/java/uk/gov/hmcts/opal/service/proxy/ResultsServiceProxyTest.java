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
import uk.gov.hmcts.opal.service.legacy.LegacyResultService;

@ExtendWith(MockitoExtension.class)
class ResultsServiceProxyTest {
    @Mock private LegacyResultService legacyResultService;
    @Mock private OpalResultService opalResultService;
    @Mock private DynamicConfigService dynamicConfigService;
    @InjectMocks private ResultsServiceProxy proxy;

    @Test
    void routesLegacyRequestsToLegacyService() {
        // Given
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        // When
        proxy.getResult("A", true);
        proxy.getResultsByIds(List.of("A"), true, false, true, false, true);

        // Then
        verify(legacyResultService).getResult("A", true);

        // Then
        verify(legacyResultService).getResultsByIds(List.of("A"), true, false, true, false, true);
    }

    @Test
    void routesOpalRequestsToOpalService() {
        // Given
        when(dynamicConfigService.isLegacyMode()).thenReturn(false);

        // When
        proxy.getResult("A", true);
        proxy.getResultsByIds(List.of("A"), true, false, true, false, true);

        // Then
        verify(opalResultService).getResult("A", true);

        // Then
        verify(opalResultService).getResultsByIds(List.of("A"), true, false, true, false, true);
    }
}
