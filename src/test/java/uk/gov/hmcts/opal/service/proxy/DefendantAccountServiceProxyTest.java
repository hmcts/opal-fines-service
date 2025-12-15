package uk.gov.hmcts.opal.service.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;

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
        testSearchDefendantAccounts(targetService, otherService);
        testGetPaymentTerms(targetService, otherService);
        testGetEnforcementStatus(targetService, otherService);
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
        assertEquals(entity, headerSummaryResult);
    }

    void testSearchDefendantAccounts(DefendantAccountServiceInterface targetService,
        DefendantAccountServiceInterface otherService) {
        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto expected = new DefendantAccountSearchResultsDto();

        when(targetService.searchDefendantAccounts(dto)).thenReturn(expected);

        DefendantAccountSearchResultsDto result = serviceProxy.searchDefendantAccounts(dto);

        verify(targetService).searchDefendantAccounts(dto);
        verifyNoInteractions(otherService);
        assertEquals(expected, result);
    }

    void testGetPaymentTerms(DefendantAccountServiceInterface targetService,
        DefendantAccountServiceInterface otherService) {

        GetDefendantAccountPaymentTermsResponse expected = new GetDefendantAccountPaymentTermsResponse();

        when(targetService.getPaymentTerms(77L)).thenReturn(expected);

        GetDefendantAccountPaymentTermsResponse result = serviceProxy.getPaymentTerms(77L);

        verify(targetService).getPaymentTerms(77L);
        verifyNoInteractions(otherService);
        assertEquals(expected, result);
    }

    void testGetEnforcementStatus(DefendantAccountServiceInterface targetService,
                              DefendantAccountServiceInterface otherService) {
        // Given: a Entity is returned from the target service
        EnforcementStatus entity = EnforcementStatus.builder()
            .build();
        when(targetService.getEnforcementStatus(anyLong())).thenReturn(entity);

        EnforcementStatus headerSummaryResult = serviceProxy.getEnforcementStatus(1L);

        // Then: target service should be used, and the returned entity should be as expected
        verify(targetService).getHeaderSummary(1L);
        verifyNoInteractions(otherService);
        assertEquals(entity, headerSummaryResult);
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
    void shouldDelegateSearchToLegacyServiceWhenInLegacyMode() {

        setMode(LEGACY);
        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto expected = new DefendantAccountSearchResultsDto();

        when(legacyService.searchDefendantAccounts(dto)).thenReturn(expected);

        DefendantAccountSearchResultsDto result = serviceProxy.searchDefendantAccounts(dto);

        verify(legacyService).searchDefendantAccounts(dto);
        verifyNoInteractions(opalService);
        assertEquals(expected, result);
    }

    @Test
    void shouldDelegateSearchToOpalServiceWhenInOpalMode() {

        setMode(OPAL);
        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto expected = new DefendantAccountSearchResultsDto();

        when(opalService.searchDefendantAccounts(dto)).thenReturn(expected);

        DefendantAccountSearchResultsDto result = serviceProxy.searchDefendantAccounts(dto);

        verify(opalService).searchDefendantAccounts(dto);
        verifyNoInteractions(legacyService);
        assertEquals(expected, result);
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

    @Test
    void shouldDelegateAddEnforcementToLegacyServiceWhenInLegacyMode() {
        // arrange
        setMode(LEGACY);

        long defendantAccountId = 77L;
        String businessUnitId = "10";
        String businessUnitUserId = "BU-USER";
        String ifMatch = "\"3\"";
        String auth = "Bearer abc";
        AddDefendantAccountEnforcementRequest req =
            mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse expected = AddEnforcementResponse.builder()
            .enforcementId("ENF-L")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(legacyService.addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req))
            .thenReturn(expected);

        // act
        AddEnforcementResponse result =
            serviceProxy.addEnforcement(defendantAccountId, businessUnitId,
                businessUnitUserId, ifMatch, auth, req);

        // assert
        verify(legacyService).addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req);
        verifyNoInteractions(opalService);
        assertEquals(expected, result);
    }

    @Test
    void shouldDelegateAddEnforcementToOpalServiceWhenInOpalMode() {
        // arrange
        setMode(OPAL);

        long defendantAccountId = 77L;
        String businessUnitId = "10";
        String businessUnitUserId = "BU-USER";
        String ifMatch = "\"3\"";
        String auth = "Bearer abc";
        AddDefendantAccountEnforcementRequest req =
            mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse expected = AddEnforcementResponse.builder()
            .enforcementId("ENF-O")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(opalService.addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req))
            .thenReturn(expected);

        // act
        AddEnforcementResponse result =
            serviceProxy.addEnforcement(defendantAccountId, businessUnitId,
                businessUnitUserId, ifMatch, auth, req);

        // assert
        verify(opalService).addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req);
        verifyNoInteractions(legacyService);
        assertEquals(expected, result);
    }


}
