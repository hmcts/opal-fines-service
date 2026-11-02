package uk.gov.hmcts.opal.service.proxy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;
import uk.gov.hmcts.opal.service.legacy.LegacyLocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalLocalJusticeAreaService;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaServiceProxyTest {

    @Mock
    private OpalLocalJusticeAreaService opalLocalJusticeAreaService;

    @Mock
    private LegacyLocalJusticeAreaService legacyLocalJusticeAreaService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private LocalJusticeAreaServiceProxy proxy;

    @Test
    void getLocalJusticeAreaById_usesLegacyServiceInLegacyMode() {
        LocalJusticeArea expected = new LocalJusticeArea();
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);
        when(legacyLocalJusticeAreaService.getLocalJusticeAreaById((short) 1)).thenReturn(expected);

        LocalJusticeArea result = proxy.getLocalJusticeAreaById((short) 1);

        assertSame(expected, result);
        verify(legacyLocalJusticeAreaService).getLocalJusticeAreaById((short) 1);
        verifyNoInteractions(opalLocalJusticeAreaService);
    }

    @Test
    void getLocalJusticeAreaRefData_usesOpalServiceInOpalMode() {
        List<LocalJusticeAreaType> ljaTypes = List.of(LocalJusticeAreaType.SJCRT);
        LocalJusticeAreasResponse expected = new LocalJusticeAreasResponse();
        when(dynamicConfigService.isLegacyMode()).thenReturn(false);
        when(opalLocalJusticeAreaService.getLocalJusticeAreaRefData("court", ljaTypes)).thenReturn(expected);

        LocalJusticeAreasResponse result = proxy.getLocalJusticeAreaRefData("court", ljaTypes);

        assertSame(expected, result);
        verify(opalLocalJusticeAreaService).getLocalJusticeAreaRefData("court", ljaTypes);
        verifyNoInteractions(legacyLocalJusticeAreaService);
    }
}
