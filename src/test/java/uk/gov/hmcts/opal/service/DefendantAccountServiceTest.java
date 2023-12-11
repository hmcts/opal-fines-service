package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @InjectMocks
    private DefendantAccountService defendantAccountService;

    @BeforeEach
    void setUp() {
        AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDefendantAccount() {
        // Arrange
        AccountEnquiryDto request = AccountEnquiryDto.builder().accountNumber("12345").businessUnitId(
            Short.valueOf("123")).build();

        DefendantAccountEntity mockEntity = new DefendantAccountEntity();
        when(defendantAccountRepository.findByBusinessUnitIdAndAccountNumber(Short.valueOf("123"), "12345"))
            .thenReturn(mockEntity);

        // Act
        DefendantAccountEntity result = defendantAccountService.getDefendantAccount(request);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1)).findByBusinessUnitIdAndAccountNumber(
            Short.valueOf("123"), "12345");
    }

    @Test
    void testPutDefendantAccount() {
        // Arrange
        DefendantAccountEntity mockEntity = new DefendantAccountEntity();
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class)))
            .thenReturn(mockEntity);

        // Act
        DefendantAccountEntity result = defendantAccountService.putDefendantAccount(mockEntity);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1)).save(mockEntity);
    }

    @Test
    void testGetDefendantAccounts() {
        // Arrange

        List<DefendantAccountEntity> mockEntity = List.of(new DefendantAccountEntity());
        when(defendantAccountRepository.findAllByBusinessUnitId(Short.valueOf("123")))
            .thenReturn(mockEntity);

        // Act
        List<DefendantAccountEntity> result = defendantAccountService.getDefendantAccountsByBusinessUnit((short) 123);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1)).findAllByBusinessUnitId(Short.valueOf("123"));
    }

    @Test
    void testSearchDefendantAccounts() {
        // Arrange
        AccountSearchDto mockSearch = AccountSearchDto.builder().build();
        AccountSearchResultsDto expectedResponse =  AccountSearchResultsDto.builder()
            .searchResults(List.of(AccountSummaryDto.builder().build()))
            .totalCount(999)
            .cursor(0)
            .build();

        // Act
        AccountSearchResultsDto result = defendantAccountService.searchDefendantAccounts(mockSearch);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void testSearchDefendantAccountsTemporary() {
        // Arrange
        AccountSearchDto mockSearch = AccountSearchDto.builder().court("test").build();
        AccountSearchResultsDto expectedResponse =  AccountSearchResultsDto.builder()
            .searchResults(List.of(AccountSummaryDto.builder().build()))
            .totalCount(999)
            .cursor(0)
            .build();

        // Act
        AccountSearchResultsDto result = defendantAccountService.searchDefendantAccounts(mockSearch);

        // Assert
        assertNotNull(result);
    }
}
