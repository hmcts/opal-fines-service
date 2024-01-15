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
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.entity.BusinessUnitsEntity;
import uk.gov.hmcts.opal.entity.CourtsEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummary;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

        assertNotNull(defendantAccountService.toDto(new TestDefendantAccountSummary()));
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
    void testGetAccountDetailsByDefendantAccountId() {

        //arrange
        DefendantAccountPartiesEntity mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntity();
        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesRepository.findByDefendantAccountDefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);


        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(buildPaymentTermsEntity());

        when(enforcersRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntity());

        //act
        AccountDetailsDto result = defendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(buildAccountDetailsDto(), result);


    }

    @Test
    void testGetAccountDetailsByDefendantAccountId_PaymentByDate() {

        //arrange
        DefendantAccountPartiesEntity mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntity();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesRepository.findByDefendantAccountDefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);

        PaymentTermsEntity paymentTermsEntity = buildPaymentTermsEntity();
        paymentTermsEntity.setTermsTypeCode("B");

        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(paymentTermsEntity);

        when(enforcersRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntity());

        AccountDetailsDto expectedDetails = buildAccountDetailsDto();
        expectedDetails.setPaymentDetails(LocalDate.of(2012, 1,1).toString() + " By Date");
        expectedDetails.setCommencing(null);

        //act
        AccountDetailsDto result = defendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(expectedDetails, result);

    }

    @Test
    void testGetAccountDetailsByDefendantAccountId_PaymentPaid() {

        //arrange
        DefendantAccountPartiesEntity mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntity();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesRepository.findByDefendantAccountDefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);

        PaymentTermsEntity paymentTermsEntity = buildPaymentTermsEntity();
        paymentTermsEntity.setTermsTypeCode("P");

        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(paymentTermsEntity);

        when(enforcersRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntity());

        AccountDetailsDto expectedDetails = buildAccountDetailsDto();
        expectedDetails.setPaymentDetails("Paid");
        expectedDetails.setCommencing(null);

        //act
        AccountDetailsDto result = defendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(expectedDetails, result);
    }

    @Test
    void testGetAccountDetailsByDefendantAccountId_Organisation() {

        //arrange
        DefendantAccountPartiesEntity mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntity();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());

        PartyEntity partyEntity = buildPartyEntity();
        partyEntity.setOrganisationName("The Bank of England");

        mockDefendantAccountPartiesEntity.setParty(partyEntity);

        when(defendantAccountPartiesRepository.findByDefendantAccountDefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);

        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(buildPaymentTermsEntity());

        when(enforcersRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntity());

        AccountDetailsDto expectedDetails = buildAccountDetailsDto();
        expectedDetails.setFullName("The Bank of England");

        //act
        AccountDetailsDto result = defendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(expectedDetails, result);
    }

    @Test
    void testGetAccountDetailsByAccountSummaryTemporary() {
        defendantAccountService.getAccountDetailsByDefendantAccountId(0L);
    }

    public static AccountDetailsDto buildAccountDetailsDto() {

        return AccountDetailsDto.builder()
            .defendantAccountId(1000L)
            .accountNumber("100")
            .fullName("Mr John Smith")
            .accountCT("CT")
            .address("1 High Street, Westminster, London")
            .postCode("W1 1AA")
            .dob(LocalDate.of(1979,12,12))
            .detailsChanged(LocalDate.of(2012, 1,1))
            .lastCourtAppAndCourtCode(LocalDate.of(2012, 1,1).toString()
                                          + " " + 1212)
            .lastMovement(LocalDate.of(2012, 1,1))
            .commentField(List.of("Comment1", "Comment2"))
            .pcr("123456")
            .paymentDetails("100.0 / PCM")
            .lumpSum(BigDecimal.valueOf(100.00))
            .commencing(LocalDate.of(2012, 1,1))
            .daysInDefault(10)
            .sentencedDate(LocalDate.of(2012, 1,1))
            .lastEnforcement("ENF")
            .override("OVER")
            .enforcer((short) 123)
            .enforcementCourt(1)
            .imposed(BigDecimal.valueOf(200.00))
            .amountPaid(BigDecimal.valueOf(100.00))
            .balance(BigDecimal.valueOf(100.00))
            .build();
    }

    public static DefendantAccountEntity buildDefendantAccountEntity() {

        BusinessUnitsEntity businessUnitEntity = BusinessUnitsEntity.builder()
            .businessUnitName("CT")
            .build();

        CourtsEntity courtsEntity1 = CourtsEntity.builder()
            .courtCode((short) 1212)
            .build();

        CourtsEntity courtsEntity2 = CourtsEntity.builder()
            .courtCode((short) 1)
            .build();

        return DefendantAccountEntity.builder()
            .defendantAccountId(1000L)
            .accountNumber("100")
            .businessUnitId(businessUnitEntity)
            .originatorType("ACC")
            .lastChangedDate(LocalDate.of(2012, 1,1))
            .lastHearingDate(LocalDate.of(2012, 1,1))
            .lastHearingCourtId(courtsEntity1)
            .lastMovementDate(LocalDate.of(2012, 1,1))
            .prosecutorCaseReference("123456")
            .imposedHearingDate(LocalDate.of(2012, 1,1))
            .lastEnforcement("ENF")
            .enforcementOverrideResultId("OVER")
            .enforcingCourtId(courtsEntity2)
            .amountImposed(BigDecimal.valueOf(200.00))
            .amountPaid(BigDecimal.valueOf(100.00))
            .accountBalance(BigDecimal.valueOf(100.00))
            .build();

    }

    public static PartyEntity buildPartyEntity() {

        return PartyEntity.builder()
            .forenames("John")
            .surname("Smith")
            .title("Mr")
            .addressLine1("1 High Street")
            .addressLine2("Westminster")
            .addressLine3("London")
            .postcode("W1 1AA")
            .dateOfBirth(LocalDate.of(1979,12,12))
            .build();
    }

    public static PaymentTermsEntity buildPaymentTermsEntity() {

        return PaymentTermsEntity.builder()
            .termsTypeCode("I")
            .instalmentAmount(BigDecimal.valueOf(100.00))
            .instalmentPeriod("PCM")
            .effectiveDate(LocalDate.of(2012, 1,1))
            .jailDays(10)
            .instalmentLumpSum(BigDecimal.valueOf(100.00))
            .build();
    }

    public static EnforcersEntity buildEnforcersEntity() {

        return EnforcersEntity.builder()
            .enforcerCode((short)123)
            .build();
    }

    public static List<NoteEntity> buildNotesEntity() {

        List<NoteEntity> notes = new ArrayList<>();

        notes.add(NoteEntity.builder()
                      .noteType("AC")
                      .noteText("Comment1")
                      .build());

        notes.add(NoteEntity.builder()
                      .noteType("AC")
                      .noteText("Comment2")
                      .build());
        return notes;
    }

    private class TestDefendantAccountSummary implements DefendantAccountSummary {

        @Override
        public Long getDefendantAccountId() {
            return 0L;
        }

        @Override
        public String getAccountNumber() {
            return "";
        }

        @Override
        public BigDecimal getAccountBalance() {
            return BigDecimal.TEN;
        }

        @Override
        public String getImposingCourtId() {
            return "";
        }

        @Override
        public Set<PartyLink> getParties() {
            return Collections.emptySet();
        }
    }

}
