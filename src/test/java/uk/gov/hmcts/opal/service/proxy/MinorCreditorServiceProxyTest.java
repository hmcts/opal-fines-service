package uk.gov.hmcts.opal.service.proxy;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMinorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalMinorCreditorService;

class MinorCreditorServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OpalMinorCreditorService opalService;

    @Mock
    private LegacyMinorCreditorService legacyService;

    @InjectMocks
    private MinorCreditorSearchProxy serviceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(MinorCreditorServiceInterface targetService, MinorCreditorServiceInterface otherService) {
        testPostSearchMinorCreditors(targetService, otherService);
    }

    void testHistoryMode(MinorCreditorServiceInterface targetService, MinorCreditorServiceInterface otherService) {
        Long id = 123L;
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            dateFrom, dateTo, List.of("amendment"));
        GetMinorCreditorHistoryResponse response = GetMinorCreditorHistoryResponse.builder().build();
        when(targetService.getMinorCreditorHistory(id, filters)).thenReturn(response);

        GetMinorCreditorHistoryResponse result = serviceProxy.getMinorCreditorHistory(id, filters);

        verify(targetService).getMinorCreditorHistory(id, filters);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(response, result);
    }

    void testPostSearchMinorCreditors(MinorCreditorServiceInterface targetService,
                                      MinorCreditorServiceInterface otherService) {

        // Given: a Entity is returned from the target service
        PostMinorCreditorAccountsSearchResponse entity = PostMinorCreditorAccountsSearchResponse.builder().build();
        when(targetService.searchMinorCreditors(any())).thenReturn(entity);

        PostMinorCreditorAccountsSearchResponse searchMinorCreditorsResult = serviceProxy.searchMinorCreditors(
            MinorCreditorSearch.builder().build());

        // Then: target service should be used, and the returned entity should be as expected
        verify(targetService).searchMinorCreditors(any());
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, searchMinorCreditorsResult);
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
    void getMinorCreditorHistory_shouldUseOpalServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testHistoryMode(opalService, legacyService);
    }

    @Test
    void getMinorCreditorHistory_shouldUseLegacyServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testHistoryMode(legacyService, opalService);
    }

}
