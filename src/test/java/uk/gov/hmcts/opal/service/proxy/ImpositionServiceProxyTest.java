package uk.gov.hmcts.opal.service.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.service.iface.ImpositionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyImpositionService;
import uk.gov.hmcts.opal.service.opal.OpalImpositionService;

class ImpositionServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OpalImpositionService opalService;

    @Mock
    private LegacyImpositionService  legacyService;

    @InjectMocks
    private ImpositionServiceProxy serviceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ImpositionServiceInterface targetService, ImpositionServiceInterface otherService) {
        getImpositions(targetService, otherService);
    }

    @Test
    void shouldUseOpalServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }

    void getImpositions(ImpositionServiceInterface targetService,
        ImpositionServiceInterface otherService) {
        GetDefendantAccountImpositionsResponse expected = GetDefendantAccountImpositionsResponse.builder().build();

        when(targetService.getImpositions(anyLong())).thenReturn(expected);

        GetDefendantAccountImpositionsResponse result = serviceProxy.getImpositions(1L);

        verify(targetService).getImpositions(1L);
        verifyNoInteractions(otherService);
        assertEquals(expected, result);
    }
}
