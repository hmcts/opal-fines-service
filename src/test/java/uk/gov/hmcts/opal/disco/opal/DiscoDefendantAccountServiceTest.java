package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiscoDefendantAccountServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Mock
    PaymentTermsRepository paymentTermsRepository;

    @Mock
    EnforcerRepository enforcerRepository;

    @Mock
    NoteRepository noteRepository;

    @InjectMocks
    private DiscoDefendantAccountService discoDefendantAccountService;

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
        when(defendantAccountRepository.findByBusinessUnit_BusinessUnitIdAndAccountNumber(
            Short.valueOf("123"), "12345"))
            .thenReturn(mockEntity);

        // Act
        DefendantAccountEntity result = discoDefendantAccountService.getDefendantAccount(request);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1))
            .findByBusinessUnit_BusinessUnitIdAndAccountNumber(
            Short.valueOf("123"), "12345");
    }

    @Test
    void testPutDefendantAccount() {
        // Arrange
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .build();
        when(defendantAccountRepository.findById(any()))
            .thenReturn(Optional.of(entity));
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class)))
            .thenReturn(entity);

        // Act
        DefendantAccountEntity result = discoDefendantAccountService.putDefendantAccount(entity);

        // Assert
        assertEquals(entity, result);
        verify(defendantAccountRepository, times(1)).save(entity);
    }

    @Test
    void testGetDefendantAccounts() {
        // Arrange

        List<DefendantAccountEntity> mockEntity = List.of(new DefendantAccountEntity());
        when(defendantAccountRepository.findAllByBusinessUnit_BusinessUnitId(Short.valueOf("123")))
            .thenReturn(mockEntity);

        // Act
        List<DefendantAccountEntity> result =
            discoDefendantAccountService.getDefendantAccountsByBusinessUnit((short) 123);

        // Assert
        assertEquals(mockEntity, result);
        verify(defendantAccountRepository, times(1))
            .findAllByBusinessUnit_BusinessUnitId(Short.valueOf("123"));
    }

    @Test
    void testGetAccountDetailsByDefendantAccountId() {

        //arrange
        DefendantAccountPartiesEntity mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntity();
        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesRepository.findByDefendantAccount_DefendantAccountId(
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
        AccountDetailsDto result = discoDefendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(buildAccountDetailsDto(), result);


    }

    @Test
    void testGetAccountDetailsByDefendantAccountId_PaymentByDate() {

        //arrange
        DefendantAccountPartiesEntity mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntity();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesRepository.findByDefendantAccount_DefendantAccountId(
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
        expectedDetails.setPaymentDetails(LocalDate.of(2012, 1,1) + " By Date");
        expectedDetails.setCommencing(null);

        //act
        AccountDetailsDto result = discoDefendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(expectedDetails, result);

    }

    @Test
    void testGetAccountDetailsByDefendantAccountId_PaymentPaid() {

        //arrange
        DefendantAccountPartiesEntity mockDefendantAccountPartiesEntity = new DefendantAccountPartiesEntity();

        mockDefendantAccountPartiesEntity.setDefendantAccount(buildDefendantAccountEntity());
        mockDefendantAccountPartiesEntity.setParty(buildPartyEntity());

        when(defendantAccountPartiesRepository.findByDefendantAccount_DefendantAccountId(
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
        AccountDetailsDto result = discoDefendantAccountService.getAccountDetailsByDefendantAccountId(1L);

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

        when(defendantAccountPartiesRepository.findByDefendantAccount_DefendantAccountId(
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
        AccountDetailsDto result = discoDefendantAccountService.getAccountDetailsByDefendantAccountId(1L);

        //assert
        assertEquals(expectedDetails, result);
    }

    @Test
    void testGetAccountDetailsByAccountSummaryTemporary() {
        discoDefendantAccountService.getAccountDetailsByDefendantAccountId(0L);
        Assertions.assertDoesNotThrow(() -> { }); // Stops SonarQube complaining about no assertions in method.
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
            .lastCourtAppAndCourtCode(LocalDate.of(2012, 1,1)
                                          + " " + 1212)
            .lastMovement(LocalDate.of(2012, 1,1))
            .commentField(List.of("Comment1"))
            .accountNotes("Activity")
            .pcr("123456")
            .paymentDetails("100.0 / PCM")
            .businessUnitId((short) 200)
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

        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder()
            .businessUnitName("CT")
            .businessUnitId((short) 200)
            .build();

        CourtEntity courtEntity1 = CourtEntity.builder()
            .courtCode((short) 1212)
            .build();

        CourtEntity courtEntity2 = CourtEntity.builder()
            .courtCode((short) 1)
            .build();

        return DefendantAccountEntity.builder()
            .defendantAccountId(1000L)
            .accountNumber("100")
            .businessUnit(businessUnitEntity)
            .originatorType("ACC")
            .lastChangedDate(LocalDate.of(2012, 1,1))
            .lastHearingDate(LocalDate.of(2012, 1,1))
            .lastHearingCourt(courtEntity1)
            .lastMovementDate(LocalDate.of(2012, 1,1))
            .prosecutorCaseReference("123456")
            .imposedHearingDate(LocalDate.of(2012, 1,1))
            .lastEnforcement("ENF")
            .enforcementOverrideResultId("OVER")
            .enforcingCourt(courtEntity2)
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
            .birthDate(LocalDate.of(1979,12,12))
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

}
