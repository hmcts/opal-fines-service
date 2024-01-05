package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.entity.BusinessUnitsEntity;
import uk.gov.hmcts.opal.entity.CourtsEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.EnforcersEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcersRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Mock
    DebtorDetailRepository debtorDetailRepository;

    @Mock
    PaymentTermsRepository paymentTermsRepository;

    @Mock
    EnforcersRepository enforcersRepository;

    @Mock
    NoteRepository noteRepository;

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
        when(defendantAccountRepository.findByBusinessUnitId_BusinessUnitIdAndAccountNumber(
            Short.valueOf("123"), "12345"))
            .thenReturn(mockEntity);

        // Act
        DefendantAccountEntity result = defendantAccountService.getDefendantAccount(request);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1))
            .findByBusinessUnitId_BusinessUnitIdAndAccountNumber(
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
        when(defendantAccountRepository.findAllByBusinessUnitId_BusinessUnitId(Short.valueOf("123")))
            .thenReturn(mockEntity);

        // Act
        List<DefendantAccountEntity> result = defendantAccountService.getDefendantAccountsByBusinessUnit((short) 123);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1))
            .findAllByBusinessUnitId_BusinessUnitId(Short.valueOf("123"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDefendantAccounts() {
        // Arrange
        AccountSearchResultsDto expectedResponse =  AccountSearchResultsDto.builder()
            .searchResults(List.of(AccountSummaryDto.builder().build()))
            .totalCount(999L)
            .cursor(1)
            .build();
        Page<AccountSummaryDto> mockPage = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 999L);
        when(defendantAccountRepository.findBy(any(Specification.class), any()))
            .thenReturn(mockPage);

        // Act
        AccountSearchResultsDto result = defendantAccountService.searchDefendantAccounts(
            AccountSearchDto.builder().build());

        // Assert
        assertEquals(expectedResponse.getTotalCount(), result.getTotalCount());
    }

    @Test
    void testSearchDefendantAccountsTemporary() {
        // Arrange
        AccountSearchDto mockSearch = AccountSearchDto.builder().court("test").build();

        // Act
        AccountSearchResultsDto result = defendantAccountService.searchDefendantAccounts(mockSearch);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getSearchResults().size());
        assertEquals(100, result.getCount());
        assertEquals(100, result.getPageSize());
        assertEquals(100, result.getTotalCount());
    }

    @Test
    void testGetAccountDetailsByAccountSummaryTemporary() {

        DefendantAccountEntity mockDefendantAccount = new DefendantAccountEntity();
        mockDefendantAccount.setDefendantAccountId(1L);
        mockDefendantAccount.setBusinessUnitId(new BusinessUnitsEntity());
        mockDefendantAccount.setLastHearingDate(LocalDate.now());
        CourtsEntity mockCourt = new CourtsEntity();
        mockCourt.setCourtCode((short)1);
        mockDefendantAccount.setLastHearingCourtId(mockCourt);
        mockDefendantAccount.setEnforcingCourtId(mockCourt);
        DefendantAccountPartiesEntity mockAccountPartyEntity = DefendantAccountPartiesEntity.builder().build();
        mockAccountPartyEntity.setDefendantAccount(mockDefendantAccount);
        PartyEntity mockPartyEntity = PartyEntity.builder().build();
        mockAccountPartyEntity.setParty(mockPartyEntity);

        when(defendantAccountPartiesRepository.findByDefendantAccountDetailsCustomQuery(
            any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(mockAccountPartyEntity);

        DebtorDetailEntity mockDebtorDetail = new DebtorDetailEntity();
        when(debtorDetailRepository.findByParty_PartyId(any())).thenReturn(mockDebtorDetail);

        PaymentTermsEntity mockPaymentTerms = new PaymentTermsEntity();
        mockPaymentTerms.setTermsTypeCode("I"); // could be "B"
        mockPaymentTerms.setJailDays(1);
        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
                .thenReturn(mockPaymentTerms);

        EnforcersEntity mockEnforcersEntity = new EnforcersEntity();

        when(enforcersRepository.findByEnforcerId(any())).thenReturn(mockEnforcersEntity);

        List<NoteEntity> mockNotes = List.of(NoteEntity.builder().build());
        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(), any())).thenReturn(mockNotes);

        defendantAccountService.getAccountDetailsByAccountSummary(constructTestAccountSummaryDto(LocalDate.now()));
    }

    @Test
    void testGetAccountDetailsByAccountSummaryTemporary2() {
        var testAccountSummary = AccountSummaryDto.builder().court("test").build();
        defendantAccountService.getAccountDetailsByAccountSummary(testAccountSummary);
    }

    private AccountSummaryDto constructTestAccountSummaryDto(final LocalDate today) {
        return AccountSummaryDto.builder()
            .accountNo("accountNameNo")
            .name("Smith, Mr JJ")
            .dateOfBirth(today)
            .addressLine1("Scotland")
            .balance(BigDecimal.valueOf(1000))
            .court("London")
            .build();
    }
}
