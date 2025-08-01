package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefendantAccountServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OpalDefendantAccountService opalService;

    @Mock
    private LegacyDefendantAccountService legacyService;

    @InjectMocks
    private DefendantAccountServiceProxy serviceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DefendantAccountServiceInterface targetService, DefendantAccountServiceInterface otherService) {
        testGetHeaderSummary(targetService, otherService);
    }

    void testGetHeaderSummary(DefendantAccountServiceInterface targetService,
                              DefendantAccountServiceInterface otherService) {
        // Given: a Entity is returned from the target service
        DefendantAccountHeaderSummary entity = DefendantAccountHeaderSummary.builder().build();
        when(targetService.getHeaderSummary(anyLong())).thenReturn(entity);

        DefendantAccountHeaderSummary headerSummaryResult = serviceProxy.getHeaderSummary(1L);

        // Then: target service should be used, and the returned entity should be as expected
        verify(targetService).getHeaderSummary(1L);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, headerSummaryResult);
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
}
