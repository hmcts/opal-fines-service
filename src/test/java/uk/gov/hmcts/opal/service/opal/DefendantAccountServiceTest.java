package uk.gov.hmcts.opal.service.opal;

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
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountCore;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntityCore;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountSummary;
import uk.gov.hmcts.opal.repository.DefendantAccountCoreRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesCoreRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountFullRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountCoreRepository defendantAccountRepository;

    // @Mock
    // private DefendantAccountLiteRepository defendantAccountLiteRepository;

    @Mock
    private DefendantAccountFullRepository defendantAccountFullRepository;

    @Mock
    DefendantAccountPartiesCoreRepository defendantAccountPartiesCoreRepository;

    @Mock
    PaymentTermsRepository paymentTermsRepository;

    @Mock
    EnforcerRepository enforcerRepository;

    @Mock
    NoteRepository noteRepository;

    @InjectMocks
    private DefendantAccountService defendantAccountService;

    @BeforeEach
    void setUp() {
        AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this);
    }

    // @Test
    // void testGetDefendantAccount() {
    //     // Arrange
    //     AccountEnquiryDto request = AccountEnquiryDto.builder().accountNumber("12345").businessUnitId(
    //         Short.valueOf("123")).build();
    //
    //     DefendantAccount.Lite mockEntity = new DefendantAccount.Lite();
    //     when(defendantAccountLiteRepository.findByBusinessUnit_BusinessUnitIdAndAccountNumber(
    //         Short.valueOf("123"), "12345"))
    //         .thenReturn(mockEntity);
    //
    //     // Act
    //     DefendantAccount.Lite result = defendantAccountService.getDefendantAccountLite(request);
    //
    //     // Assert
    //     assertEquals(mockEntity, result);
    //     verify(defendantAccountRepository, times(1))
    //         .findByBusinessUnit_BusinessUnitIdAndAccountNumber(
    //         Short.valueOf("123"), "12345");
    // }

    @Test
    void testPutDefendantAccount() {
        // Arrange
        DefendantAccountCore mockEntity = new DefendantAccountCore();
        when(defendantAccountRepository.save(any(DefendantAccountCore.class)))
            .thenReturn(mockEntity);

        // Act
        DefendantAccountCore result = defendantAccountService.putDefendantAccount(mockEntity);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1)).save(mockEntity);
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
        DefendantAccountPartiesEntityCore mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntityCore();
        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesCoreRepository.findByDefendantAccount_DefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);


        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(buildPaymentTermsEntity());

        when(enforcerRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntityComment());

        when(noteRepository.findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(any(),any()))
            .thenReturn(buildNotesEntityActivity());

        //act
        AccountDetailsDto result = defendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(buildAccountDetailsDto(), result);


    }

    @Test
    void testGetAccountDetailsByDefendantAccountId_PaymentByDate() {

        //arrange
        DefendantAccountPartiesEntityCore mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntityCore();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesCoreRepository.findByDefendantAccount_DefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);

        PaymentTermsEntity paymentTermsEntity = buildPaymentTermsEntity();
        paymentTermsEntity.setTermsTypeCode("B");

        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(paymentTermsEntity);

        when(enforcerRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntityComment());

        when(noteRepository.findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(any(),any()))
            .thenReturn(buildNotesEntityActivity());

        AccountDetailsDto expectedDetails = buildAccountDetailsDto();
        // expectedDetails.setAccountCT(null);
        expectedDetails.setPaymentDetails(LocalDate.of(2012, 1,1) + " By Date");
        expectedDetails.setCommencing(null);

        //act
        AccountDetailsDto result = defendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(expectedDetails, result);

    }

    @Test
    void testGetAccountDetailsByDefendantAccountId_PaymentPaid() {

        //arrange
        DefendantAccountPartiesEntityCore mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntityCore();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesCoreRepository.findByDefendantAccount_DefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);

        PaymentTermsEntity paymentTermsEntity = buildPaymentTermsEntity();
        paymentTermsEntity.setTermsTypeCode("P");

        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(paymentTermsEntity);

        when(enforcerRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntityComment());

        when(noteRepository.findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(any(),any()))
            .thenReturn(buildNotesEntityActivity());

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
        DefendantAccountPartiesEntityCore mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntityCore();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());

        PartyEntity partyEntity = buildPartyEntity();
        partyEntity.setOrganisationName("The Bank of England");

        mockDefendantAccountPartiesEntity.setParty(partyEntity);

        when(defendantAccountPartiesCoreRepository.findByDefendantAccount_DefendantAccountId(
            any()))
            .thenReturn(mockDefendantAccountPartiesEntity);

        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountId(any()))
            .thenReturn(buildPaymentTermsEntity());

        when(enforcerRepository.findByEnforcerId(any()))
            .thenReturn(buildEnforcersEntity());

        when(noteRepository.findByAssociatedRecordIdAndNoteType(any(),any()))
            .thenReturn(buildNotesEntityComment());

        when(noteRepository.findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(any(),any()))
            .thenReturn(buildNotesEntityActivity());

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
            // .accountCT("CT")
            .businessUnitId((short)1)
            .address("1 High Street, Westminster, London")
            .postCode("W1 1AA")
            .dob(LocalDate.of(1979,12,12))
            .detailsChanged(LocalDate.of(2012, 1,1))
            // .lastCourtAppAndCourtCode(LocalDate.of(2012, 1,1)
            //                               + " " + 1212)
            .lastMovement(LocalDate.of(2012, 1,1))
            .commentField(List.of("Comment1"))
            .accountNotes("Activity")
            .pcr("123456")
            .paymentDetails("100.0 / PCM")
            .lumpSum(BigDecimal.valueOf(100.00))
            .commencing(LocalDate.of(2012, 1,1))
            .daysInDefault(10)
            .sentencedDate(LocalDate.of(2012, 1,1))
            .lastEnforcement("ENF")
            .override("OVER")
            .enforcer((short) 123)
            .enforcementCourt(0)
            .imposed(BigDecimal.valueOf(200.00))
            .amountPaid(BigDecimal.valueOf(100.00))
            .balance(BigDecimal.valueOf(100.00))
            .build();
    }

    public static DefendantAccountCore buildDefendantAccountEntity() {

        BusinessUnit.Lite businessUnitEntity = BusinessUnit.Lite.builder()
            .businessUnitName("CT")
            .build();

        CourtEntity.Lite courtEntity1 = CourtEntity.Lite.builder()
            .courtCode((short) 1212)
            .build();

        CourtEntity.Lite courtEntity2 = CourtEntity.Lite.builder()
            .courtCode((short) 1)
            .build();

        return DefendantAccountCore.builder()
            .defendantAccountId(1000L)
            .accountNumber("100")
            .businessUnitId((short)1)
            .originatorType("ACC")
            .lastChangedDate(LocalDate.of(2012, 1,1))
            .lastHearingDate(LocalDate.of(2012, 1,1))
            .lastHearingCourtId(1L)
            .lastMovementDate(LocalDate.of(2012, 1,1))
            .prosecutorCaseReference("123456")
            .imposedHearingDate(LocalDate.of(2012, 1,1))
            .lastEnforcement("ENF")
            .enforcementOverrideResultId("OVER")
            .enforcingCourtId(2L)
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

    public static EnforcerEntity buildEnforcersEntity() {

        return EnforcerEntity.builder()
            .enforcerCode((short)123)
            .build();
    }

    public static List<NoteEntity> buildNotesEntityComment() {

        List<NoteEntity> notes = new ArrayList<>();

        notes.add(NoteEntity.builder()
                      .noteType("AC")
                      .noteText("Comment1")
                      .build());
        return notes;
    }

    public static NoteEntity buildNotesEntityActivity() {

        return NoteEntity.builder()
            .noteType("AA")
            .noteText("Activity")
            .build();
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
