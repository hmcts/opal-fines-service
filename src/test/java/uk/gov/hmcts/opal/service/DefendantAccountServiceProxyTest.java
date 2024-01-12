package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;


class DefendantAccountServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private DefendantAccountService opalDefendantAccountService;

    @Mock
    private LegacyDefendantAccountService legacyDefendantAccountService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalDefendantAccountServiceWhenModeIsNotLegacy() {
        // Given: a AccountSearchDto and the app mode is set to "opal"
        AccountSearchDto searchDto = AccountSearchDto.builder().build();
        AccountSearchResultsDto resultsDto = AccountSearchResultsDto.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalDefendantAccountService.searchDefendantAccounts(searchDto)).thenReturn(resultsDto);

        // When: searchDefendantAccounts is called on the proxy
        AccountSearchResultsDto result = defendantAccountServiceProxy.searchDefendantAccounts(searchDto);

        // Then: opalDefendantAccountService should be used, and the returned note should be as expected
        verify(opalDefendantAccountService).searchDefendantAccounts(searchDto);
        verifyNoInteractions(legacyDefendantAccountService);
        assertEquals(resultsDto, result);

        // Given
        AccountEnquiryDto enquiryDto = AccountEnquiryDto.builder().build();
        DefendantAccountEntity accountEntity = DefendantAccountEntity.builder().build();
        when(opalDefendantAccountService.getDefendantAccount(enquiryDto)).thenReturn(accountEntity);

        // When
        DefendantAccountEntity resultEntity = defendantAccountServiceProxy.getDefendantAccount(enquiryDto);

        // Then
        verify(opalDefendantAccountService).getDefendantAccount(enquiryDto);
        verifyNoInteractions(legacyDefendantAccountService);
        assertEquals(resultEntity, accountEntity);

        // Given
        AccountDetailsDto accountDetails = AccountDetailsDto.builder().build();
        when(opalDefendantAccountService.getAccountDetailsByDefendantAccountId(any(Long.class)))
            .thenReturn(accountDetails);

        // When
        AccountDetailsDto resultDetails = defendantAccountServiceProxy.getAccountDetailsByDefendantAccountId(1L);

        // Then
        verify(opalDefendantAccountService).getAccountDetailsByDefendantAccountId(1L);
        verifyNoInteractions(legacyDefendantAccountService);
        assertEquals(accountDetails, resultDetails);


        // Given
        when(opalDefendantAccountService.putDefendantAccount(accountEntity))
            .thenReturn(accountEntity);

        // When
        DefendantAccountEntity resultEntity2 = defendantAccountServiceProxy.putDefendantAccount(accountEntity);

        // Then
        verify(opalDefendantAccountService).putDefendantAccount(accountEntity);
        verifyNoInteractions(legacyDefendantAccountService);
        assertEquals(accountEntity, resultEntity2);
    }

    @Test
    void shouldUseLegacyDefendantAccountServiceWhenModeIsLegacy() {
        // Given: a AccountSearchDto and the app mode is set to "legacy"
        AccountSearchDto searchDto = AccountSearchDto.builder().build();
        AccountSearchResultsDto resultsDto = AccountSearchResultsDto.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();

        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyDefendantAccountService.searchDefendantAccounts(searchDto)).thenReturn(resultsDto);

        // When: searchDefendantAccounts is called on the proxy
        AccountSearchResultsDto result = defendantAccountServiceProxy.searchDefendantAccounts(searchDto);

        // Then: legacyDefendantAccountService should be used, and the returned note should be as expected
        verify(legacyDefendantAccountService).searchDefendantAccounts(searchDto);
        verifyNoInteractions(opalDefendantAccountService);
        assertEquals(resultsDto, result);

        // Given
        AccountEnquiryDto enquiryDto = AccountEnquiryDto.builder().build();
        DefendantAccountEntity accountEntity = DefendantAccountEntity.builder().build();
        when(legacyDefendantAccountService.getDefendantAccount(enquiryDto)).thenReturn(accountEntity);

        // When
        DefendantAccountEntity resultEntity = defendantAccountServiceProxy.getDefendantAccount(enquiryDto);

        // Then
        verify(legacyDefendantAccountService).getDefendantAccount(enquiryDto);
        verifyNoInteractions(opalDefendantAccountService);
        assertEquals(resultEntity, accountEntity);

        // Given
        AccountDetailsDto accountDetails = AccountDetailsDto.builder().build();
        when(legacyDefendantAccountService.getAccountDetailsByDefendantAccountId(any(Long.class)))
            .thenReturn(accountDetails);

        // When
        AccountDetailsDto resultDetails = defendantAccountServiceProxy.getAccountDetailsByDefendantAccountId(1L);

        // Then
        verify(legacyDefendantAccountService).getAccountDetailsByDefendantAccountId(1L);
        verifyNoInteractions(opalDefendantAccountService);
        assertEquals(accountDetails, resultDetails);


        // Given
        when(legacyDefendantAccountService.putDefendantAccount(accountEntity))
            .thenReturn(accountEntity);

        // When
        DefendantAccountEntity resultEntity2 = defendantAccountServiceProxy.putDefendantAccount(accountEntity);

        // Then
        verify(legacyDefendantAccountService).putDefendantAccount(accountEntity);
        verifyNoInteractions(opalDefendantAccountService);
        assertEquals(accountEntity, resultEntity2);
    }
}
