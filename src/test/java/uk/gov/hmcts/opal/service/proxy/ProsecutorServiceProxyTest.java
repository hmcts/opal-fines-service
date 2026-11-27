package uk.gov.hmcts.opal.service.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;
import uk.gov.hmcts.opal.service.legacy.LegacyProsecutorService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalProsecutorService;

@ExtendWith(MockitoExtension.class)
class ProsecutorServiceProxyTest {

    @Mock
    private OpalProsecutorService opalProsecutorService;

    @Mock
    private LegacyProsecutorService legacyProsecutorService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private ProsecutorServiceProxy prosecutorServiceProxy;

    @Test
    void getProsecutorByKey_usesOpalServiceInOpalMode() {
        ProsecutorsObject expected = ProsecutorsObject.builder().name("Opal").build();
        when(opalProsecutorService.getProsecutorByKey("10")).thenReturn(expected);

        assertEquals(expected, prosecutorServiceProxy.getProsecutorByKey("10"));
        verify(opalProsecutorService).getProsecutorByKey("10");
        verifyNoInteractions(legacyProsecutorService);
    }

    @Test
    void getProsecutorByKey_usesLegacyServiceInLegacyMode() {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);
        ProsecutorsObject expected = ProsecutorsObject.builder().name("Legacy").build();
        when(legacyProsecutorService.getProsecutorByKey("10")).thenReturn(expected);

        assertEquals(expected, prosecutorServiceProxy.getProsecutorByKey("10"));
        verify(legacyProsecutorService).getProsecutorByKey("10");
        verifyNoInteractions(opalProsecutorService);
    }

    @Test
    void getProsecutorsRefData_usesOpalServiceInOpalMode() {
        ProsecutorsReferenceDataResponse expected = ProsecutorsReferenceDataResponse.builder().count(1).build();
        when(opalProsecutorService.getProsecutorsRefData("opal")).thenReturn(expected);

        assertEquals(expected, prosecutorServiceProxy.getProsecutorsRefData("opal"));
        verify(opalProsecutorService).getProsecutorsRefData("opal");
        verifyNoInteractions(legacyProsecutorService);
    }

    @Test
    void getProsecutorsRefData_usesLegacyServiceInLegacyMode() {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);
        ProsecutorsReferenceDataResponse expected = ProsecutorsReferenceDataResponse.builder().count(1).build();
        when(legacyProsecutorService.getProsecutorsRefData("legacy")).thenReturn(expected);

        assertEquals(expected, prosecutorServiceProxy.getProsecutorsRefData("legacy"));
        verify(legacyProsecutorService).getProsecutorsRefData("legacy");
        verifyNoInteractions(opalProsecutorService);
    }
}
