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
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsSummaryServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacySuspenseAccountSummaryService;
import uk.gov.hmcts.opal.service.opal.OpalSuspenseAccountsSummaryService;

@ExtendWith(MockitoExtension.class)
class SuspenseAccountSummaryServiceProxyTest extends ProxyTestsBase {

    @Mock
    private OpalSuspenseAccountsSummaryService opalService;

    @Mock
    private LegacySuspenseAccountSummaryService legacyService;

    @InjectMocks
    private SuspenseAccountSummaryServiceProxy proxy;

    @Test
    void getSummary_usesOpalMode() {
        setLegacyMode(false);

        assertDelegation(opalService, legacyService);
    }

    @Test
    void getSummary_usesLegacyMode() {
        setLegacyMode(true);

        assertDelegation(legacyService, opalService);
    }

    private void assertDelegation(SuspenseAccountsSummaryServiceInterface selected,
        SuspenseAccountsSummaryServiceInterface other) {

        SuspenseItemSummaryCommonStrict expected =
            SuspenseItemSummaryCommonStrict.builder().suspenseItemId(1L).balance("100.0").build();
        when(selected.getSuspenseAccountsSummary((short) 123, 12345L)).thenReturn(expected);

        SuspenseItemSummaryCommonStrict result = proxy.getSuspenseAccountsSummary((short) 123, 12345L);

        assertEquals(expected, result);
        verify(selected).getSuspenseAccountsSummary((short) 123, 12345L);
        verifyNoInteractions(other);
    }
}
