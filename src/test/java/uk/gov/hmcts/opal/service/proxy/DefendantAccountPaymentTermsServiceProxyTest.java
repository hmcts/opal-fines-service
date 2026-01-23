package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPaymentTermsServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountPaymentTermsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefendantAccountPaymentTermsServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OpalDefendantAccountPaymentTermsService opalService;

    @Mock
    private LegacyDefendantAccountPaymentTermsService legacyService;

    @InjectMocks
    private DefendantAccountPaymentTermsServiceProxy serviceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DefendantAccountPaymentTermsServiceInterface targetService,
                  DefendantAccountPaymentTermsServiceInterface otherService) {
        testGetPaymentTerms(targetService, otherService);
    }

    void testGetPaymentTerms(DefendantAccountPaymentTermsServiceInterface targetService,
                             DefendantAccountPaymentTermsServiceInterface otherService) {

        GetDefendantAccountPaymentTermsResponse expected = new GetDefendantAccountPaymentTermsResponse();

        when(targetService.getPaymentTerms(77L)).thenReturn(expected);

        GetDefendantAccountPaymentTermsResponse result = serviceProxy.getPaymentTerms(77L);

        verify(targetService).getPaymentTerms(77L);
        verifyNoInteractions(otherService);
        assertEquals(expected, result);
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

    @Test
    void shouldDelegateGetPaymentTermsToOpalServiceWhenInOpalMode() {

        setMode(OPAL);
        GetDefendantAccountPaymentTermsResponse expected = new GetDefendantAccountPaymentTermsResponse();

        when(opalService.getPaymentTerms(77L)).thenReturn(expected);

        GetDefendantAccountPaymentTermsResponse result = serviceProxy.getPaymentTerms(77L);

        verify(opalService).getPaymentTerms(77L);
        verifyNoInteractions(legacyService);
        assertEquals(expected, result);
    }
}
