package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.disco.DiscoDefendantAccountServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyDiscoDefendantAccountService;
import uk.gov.hmcts.opal.disco.opal.DiscoDefendantAccountService;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class DiscoDefendantAccountServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private DiscoDefendantAccountService opalService;

    @Mock
    private LegacyDiscoDefendantAccountService legacyService;

    @InjectMocks
    private DiscoDefendantAccountServiceProxy discoDefendantAccountServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DiscoDefendantAccountServiceInterface targetService,
                  DiscoDefendantAccountServiceInterface otherService) {
        testGetDefendantAccount(targetService, otherService);
        testSearchDefendantAccounts(targetService, otherService);
        testGetAccountDetails(targetService, otherService);
        testPutDefendantAccount(targetService, otherService);
    }

    void testGetDefendantAccount(DiscoDefendantAccountServiceInterface targetService,
                                 DiscoDefendantAccountServiceInterface otherService) {
        // Given: a DefendantAccountEntity is returned from the target service
        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();
        AccountEnquiryDto enquiryDto = AccountEnquiryDto.builder().build();
        when(targetService.getDefendantAccount(any(AccountEnquiryDto.class))).thenReturn(entity);

        // When: getDefendantAccount is called on the proxy
        DefendantAccountEntity defendantAccountResult = discoDefendantAccountServiceProxy
            .getDefendantAccount(enquiryDto);

        // Then: target service should be used, and the returned defendantAccount should be as expected
        verify(targetService).getDefendantAccount(enquiryDto);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, defendantAccountResult);
    }

    void testSearchDefendantAccounts(DiscoDefendantAccountServiceInterface targetService,
                                     DiscoDefendantAccountServiceInterface otherService) {
        // Given: a defendantAccounts results dto result is returned from the target service
        DefendantAccountSearchResultsDto resultsDto = DefendantAccountSearchResultsDto.builder().build();
        when(targetService.searchDefendantAccounts(any())).thenReturn(resultsDto);

        // When: searchDefendantAccounts is called on the proxy
        AccountSearchDto criteria = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto listResult = discoDefendantAccountServiceProxy
            .searchDefendantAccounts(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchDefendantAccounts(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(resultsDto, listResult);
    }

    void testGetAccountDetails(DiscoDefendantAccountServiceInterface targetService,
                               DiscoDefendantAccountServiceInterface otherService) {
        // Given: a DefendantAccountEntity is returned from the target service
        AccountDetailsDto accountDetails = AccountDetailsDto.builder().build();
        when(targetService.getAccountDetailsByDefendantAccountId(anyLong())).thenReturn(accountDetails);

        // When: getDefendantAccount is called on the proxy
        AccountDetailsDto defendantAccountResult = discoDefendantAccountServiceProxy
            .getAccountDetailsByDefendantAccountId(1L);

        // Then: target service should be used, and the returned defendantAccount should be as expected
        verify(targetService).getAccountDetailsByDefendantAccountId(1L);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(accountDetails, defendantAccountResult);
    }

    void testPutDefendantAccount(DiscoDefendantAccountServiceInterface targetService,
                                 DiscoDefendantAccountServiceInterface otherService) {
        // Given: a DefendantAccountEntity is returned from the target service
        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();
        when(targetService.putDefendantAccount(any(DefendantAccountEntity.class))).thenReturn(entity);

        // When: putDefendantAccount is called on the proxy
        DefendantAccountEntity defendantAccountResult = discoDefendantAccountServiceProxy.putDefendantAccount(entity);

        // Then: target service should be used, and the returned defendantAccount should be as expected
        verify(targetService).putDefendantAccount(entity);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, defendantAccountResult);
    }

    @Test
    void shouldUseOpalDefendantAccountServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyDefendantAccountServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
