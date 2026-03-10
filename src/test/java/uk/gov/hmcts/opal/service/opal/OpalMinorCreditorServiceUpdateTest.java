package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountUpdateMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.util.VersionUtils;

@ExtendWith(MockitoExtension.class)
class OpalMinorCreditorServiceUpdateTest {

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private MinorCreditorRepository minorCreditorRepository;

    @Mock
    private MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;

    @Mock
    private AmendmentService amendmentService;

    @Mock
    private MinorCreditorAccountUpdateMapper minorCreditorAccountUpdateMapper;

    @Mock
    private MinorCreditorAccountResponseMapper minorCreditorAccountResponseMapper;

    @InjectMocks
    private OpalMinorCreditorService service;

    @Test
    void updateMinorCreditorAccount_success_updatesHoldAndVersionAndReturnsResponse() {
        // Arrange
        Long accountId = 101L;
        BigInteger etag = BigInteger.valueOf(5L);
        String postedBy = "test.user@hmcts.net";

        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .businessUnitId((short) 77)
            .minorCreditorPartyId(201L)
            .versionNumber(5L)
            .holdPayout(false)
            .payByBacs(true)
            .bankAccountName("A NAME")
            .bankSortCode("112233")
            .bankAccountNumber("12345678")
            .bankAccountReference("REF")
            .build();

        PartyEntity party = PartyEntity.builder()
            .partyId(201L)
            .organisation(false)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .addressLine1("1 Any Street")
            .postcode("AB1 2CD")
            .build();

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                              .partyId("201")
                              .organisationFlag(false)
                              .individualDetails(new IndividualDetailsCommon()
                                                     .title("Dr")
                                                     .forenames("Jane")
                                                     .surname("Updated")
                                                     .dateOfBirth("2000-02-01")
                                                     .age("24")
                                                     .nationalInsuranceNumber("QQ123456C")))
            .address(new AddressDetailsCommon()
                         .addressLine1("100 New Road")
                         .addressLine2("Town")
                         .postcode("ZZ1 1ZZ"))
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(partyRepository.findById(201L)).thenReturn(Optional.of(party));
        when(partyRepository.save(any(PartyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(creditorAccountRepository.saveAndFlush(any(CreditorAccountEntity.Lite.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        MinorCreditorAccountResponse mappedResponse = new MinorCreditorAccountResponse();
        mappedResponse.setCreditorAccountId(accountId);
        mappedResponse.setPayment(new MinorCreditorAccountResponseMinorCreditorPayment().holdPayment(true));
        when(minorCreditorAccountResponseMapper.toMinorCreditorAccountResponse(account, party))
            .thenReturn(mappedResponse);
        doAnswer(invocation -> {
            PartyEntity target = invocation.getArgument(2);
            target.setTitle("Dr");
            target.setForenames("Jane");
            target.setSurname("Updated");
            target.setAddressLine1("100 New Road");
            target.setAddressLine2("Town");
            target.setPostcode("ZZ1 1ZZ");
            return null;
        }).when(minorCreditorAccountUpdateMapper).updateParty(any(), any(), any());

        // Act
        MinorCreditorAccountResponse response;
        try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
            response = service.updateMinorCreditorAccount(accountId, request, etag, postedBy);
            versionUtils.verify(() -> VersionUtils.verifyIfMatch(
                eq(account), eq(etag), eq(accountId), eq("updateMinorCreditorAccount")));
        }

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getCreditorAccountId());
        assertNotNull(response.getPayment());
        assertEquals(true, response.getPayment().getHoldPayment());
        assertEquals(BigInteger.valueOf(5L), response.getVersion());
        ArgumentCaptor<CreditorAccountEntity.Lite> entityCaptor =
            ArgumentCaptor.forClass(CreditorAccountEntity.Lite.class);
        verify(creditorAccountRepository).saveAndFlush(entityCaptor.capture());
        CreditorAccountEntity.Lite savedEntity = entityCaptor.getValue();
        assertNotNull(savedEntity);
        assertEquals(accountId, savedEntity.getCreditorAccountId());
        assertEquals(CreditorAccountType.MN, savedEntity.getCreditorAccountType());
        assertEquals((short) 77, savedEntity.getBusinessUnitId());
        assertEquals(201L, savedEntity.getMinorCreditorPartyId());
        assertTrue(savedEntity.isHoldPayout());
        assertEquals(BigInteger.valueOf(5L), savedEntity.getVersion());
        ArgumentCaptor<PartyEntity> partyCaptor = ArgumentCaptor.forClass(PartyEntity.class);
        verify(partyRepository).save(partyCaptor.capture());
        PartyEntity savedParty = partyCaptor.getValue();
        assertEquals("Dr", savedParty.getTitle());
        assertEquals("Jane", savedParty.getForenames());
        assertEquals("Updated", savedParty.getSurname());
        assertEquals("100 New Road", savedParty.getAddressLine1());
        assertEquals("ZZ1 1ZZ", savedParty.getPostcode());
        verify(amendmentService).auditInitialiseStoredProc(accountId, RecordType.CREDITOR_ACCOUNTS);
        verify(amendmentService).auditFinaliseStoredProc(
            accountId, RecordType.CREDITOR_ACCOUNTS, (short) 77, postedBy, null, "ACCOUNT_ENQUIRY");
    }

    @Test
    void updateMinorCreditorAccount_missingPaymentGroup_throwsIllegalArgumentException() {
        // Arrange
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .address(new AddressDetailsCommon());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_nullRequest_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(1L, null, BigInteger.ONE, "test.user")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_nullHoldPayment_throwsIllegalArgumentException() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_missingPartyDetails_throwsIllegalArgumentException() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_missingAddress_throwsIllegalArgumentException() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_missingCreditorAccount_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 999L;
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, BigInteger.ONE, "test.user")
        );
    }

    @Test
    void updateMinorCreditorAccount_nonMinorCreditor_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 102L;
        CreditorAccountEntity.Lite majorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MJ)
            .versionNumber(1L)
            .build();

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(majorAccount));

        // Act + Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, BigInteger.ONE, "test.user")
        );
    }

    @Test
    void updateMinorCreditorAccount_nullCreditorAccountType_throwsEntityNotFoundException() {
        Long accountId = 106L;
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(null)
            .versionNumber(1L)
            .build();

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(
            EntityNotFoundException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, BigInteger.ONE, "test.user")
        );
    }

    @Test
    void updateMinorCreditorAccount_nullVersion_throwsResourceConflictException() {
        Long accountId = 107L;
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(201L)
            .versionNumber(null)
            .build();

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("201").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(
            ResourceConflictException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, BigInteger.ONE, "test.user")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingParty_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 103L;
        CreditorAccountEntity.Lite minorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(999L)
            .businessUnitId((short) 55)
            .versionNumber(3L)
            .build();

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("999").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(false));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(minorAccount));
        when(partyRepository.findById(999L)).thenReturn(Optional.empty());
        BigInteger currentVersion = BigInteger.valueOf(3L);

        // Act + Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, currentVersion, "test.user")
        );
    }

    @Test
    void updateMinorCreditorAccount_partyIdMismatch_throwsIllegalArgumentException() {
        Long accountId = 104L;
        CreditorAccountEntity.Lite minorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(201L)
            .businessUnitId((short) 55)
            .versionNumber(3L)
            .build();

        PartyEntity party = PartyEntity.builder().partyId(201L).organisation(false).build();
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("202").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(false));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(minorAccount));
        when(partyRepository.findById(201L)).thenReturn(Optional.of(party));
        BigInteger currentVersion = BigInteger.valueOf(3L);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, currentVersion, "test.user")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingPartyId_throwsIllegalArgumentException() {
        Long accountId = 105L;
        CreditorAccountEntity.Lite minorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(201L)
            .businessUnitId((short) 55)
            .versionNumber(3L)
            .build();

        PartyEntity party = PartyEntity.builder().partyId(201L).organisation(false).build();
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(false));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(minorAccount));
        when(partyRepository.findById(201L)).thenReturn(Optional.of(party));
        BigInteger currentVersion = BigInteger.valueOf(3L);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, currentVersion, "test.user")
        );

        assertEquals("party_details.party_id must be provided", exception.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_blankPartyId_throwsIllegalArgumentException() {
        Long accountId = 108L;
        CreditorAccountEntity.Lite minorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(201L)
            .businessUnitId((short) 55)
            .versionNumber(3L)
            .build();

        PartyEntity party = PartyEntity.builder().partyId(201L).organisation(false).build();
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId(" ").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(false));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(minorAccount));
        when(partyRepository.findById(201L)).thenReturn(Optional.of(party));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, BigInteger.valueOf(3L), "test.user")
        );

        assertEquals("party_details.party_id must be provided", exception.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_invalidPartyIdFormat_throwsIllegalArgumentException() {
        Long accountId = 109L;
        CreditorAccountEntity.Lite minorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(201L)
            .businessUnitId((short) 55)
            .versionNumber(3L)
            .build();

        PartyEntity party = PartyEntity.builder().partyId(201L).organisation(false).build();
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("ABC").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(false));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(minorAccount));
        when(partyRepository.findById(201L)).thenReturn(Optional.of(party));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, BigInteger.valueOf(3L), "test.user")
        );

        assertEquals("Invalid party_details.party_id format", exception.getMessage());
    }
}
