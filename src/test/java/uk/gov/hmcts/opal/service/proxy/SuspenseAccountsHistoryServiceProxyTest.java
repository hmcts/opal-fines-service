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
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsHistoryServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacySuspenseAccountsHistoryService;
import uk.gov.hmcts.opal.service.opal.OpalSuspenseAccountsHistoryService;

@ExtendWith(MockitoExtension.class)
class SuspenseAccountsHistoryServiceProxyTest extends ProxyTestsBase {

    @Mock
    private OpalSuspenseAccountsHistoryService opalService;

    @Mock
    private LegacySuspenseAccountsHistoryService legacyService;

    @InjectMocks
    private SuspenseAccountsHistoryServiceProxy proxy;

    @Test
    void getHistory_usesOpalMode() {
        setLegacyMode(false);

        assertDelegation(opalService, legacyService);
    }

    @Test
    void getHistory_usesLegacyMode() {
        setLegacyMode(true);

        assertDelegation(legacyService, opalService);
    }

    private void assertDelegation(SuspenseAccountsHistoryServiceInterface selected,
        SuspenseAccountsHistoryServiceInterface other) {

        SuspenseItemHistoryCommonStrict expected =
            new SuspenseItemHistoryCommonStrict().businessUnitId(123).suspenseItemId(1);
        when(selected.getSuspenseItemHistory(123L, 12345L)).thenReturn(expected);

        SuspenseItemHistoryCommonStrict result = proxy.getSuspenseItemHistory(123L, 12345L);

        assertEquals(expected, result);
        verify(selected).getSuspenseItemHistory(123L, 12345L);
        verifyNoInteractions(other);
    }
}
