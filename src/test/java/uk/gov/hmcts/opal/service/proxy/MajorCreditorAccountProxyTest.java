package uk.gov.hmcts.opal.service.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.service.iface.MajorCreditorAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService;
import uk.gov.hmcts.opal.service.opal.OpalMajorCreditorAccountService;

class MajorCreditorAccountProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OpalMajorCreditorAccountService opalService;

    @Mock
    private LegacyMajorCreditorAccountService legacyService;

    @InjectMocks
    private MajorCreditorAccountProxy serviceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getAtAGlance_usesOpalServiceWhenModeIsNotLegacy() {
        setLegacyMode(false);
        GetMajorCreditorAccountAtAGlanceResponse response = new GetMajorCreditorAccountAtAGlanceResponse();
        when(opalService.getAtAGlance(123L)).thenReturn(response);

        GetMajorCreditorAccountAtAGlanceResponse result = serviceProxy.getAtAGlance(123L);

        assertEquals(response, result);
        verify(opalService).getAtAGlance(123L);
        verifyNoInteractions(legacyService);
    }

    @Test
    void getAtAGlance_usesLegacyServiceWhenModeIsLegacy() {
        setLegacyMode(true);
        GetMajorCreditorAccountAtAGlanceResponse response = new GetMajorCreditorAccountAtAGlanceResponse();
        when(legacyService.getAtAGlance(123L)).thenReturn(response);

        GetMajorCreditorAccountAtAGlanceResponse result = serviceProxy.getAtAGlance(123L);

        assertEquals(response, result);
        verify(legacyService).getAtAGlance(123L);
        verifyNoInteractions(opalService);
    }

    @Test
    void getHeaderSummary_usesOpalServiceWhenModeIsNotLegacy() {
        setLegacyMode(false);
        testHeaderSummaryMode(opalService, legacyService);
    }

    @Test
    void getHeaderSummary_usesLegacyServiceWhenModeIsLegacy() {
        setLegacyMode(true);
        testHeaderSummaryMode(legacyService, opalService);
    }

    @Test
    void getHistory_usesOpalServiceWhenModeIsNotLegacy() {
        setLegacyMode(false);
        testHistoryMode(opalService, legacyService);
    }

    @Test
    void getHistory_usesLegacyServiceWhenModeIsLegacy() {
        setLegacyMode(true);
        testHistoryMode(legacyService, opalService);
    }

    private void testHeaderSummaryMode(
        MajorCreditorAccountServiceInterface targetService,
        MajorCreditorAccountServiceInterface otherService
    ) {
        GetMajorCreditorAccountHeaderSummaryResponse response =
            new GetMajorCreditorAccountHeaderSummaryResponse();
        when(targetService.getHeaderSummary(123L)).thenReturn(response);

        GetMajorCreditorAccountHeaderSummaryResponse result = serviceProxy.getHeaderSummary(123L);

        assertEquals(response, result);
        verify(targetService).getHeaderSummary(123L);
        verifyNoInteractions(otherService);
    }

    private void testHistoryMode(
        MajorCreditorAccountServiceInterface targetService,
        MajorCreditorAccountServiceInterface otherService
    ) {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        List<String> itemTypes = List.of("financial");
        GetMajorCreditorHistoryResponse response = GetMajorCreditorHistoryResponse.builder().build();
        when(targetService.getHistory(123L, dateFrom, dateTo, itemTypes)).thenReturn(response);

        GetMajorCreditorHistoryResponse result = serviceProxy.getHistory(123L, dateFrom, dateTo, itemTypes);

        assertEquals(response, result);
        verify(targetService).getHistory(123L, dateFrom, dateTo, itemTypes);
        verifyNoInteractions(otherService);
    }
}
