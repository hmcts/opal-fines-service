package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;

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
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(partyRepository.findById(201L)).thenReturn(Optional.of(party));
        when(creditorAccountRepository.save(any(CreditorAccountEntity.Lite.class))).thenReturn(account);

        // Act
        MinorCreditorAccountResponse response =
            service.updateMinorCreditorAccount(accountId, request, etag, postedBy);

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getCreditorAccountId());
        assertNotNull(response.getPayment());
        assertEquals(true, response.getPayment().getHoldPayment());
        assertEquals(BigInteger.valueOf(6L), response.getVersion());
        verify(amendmentService).auditInitialiseStoredProc(accountId, RecordType.CREDITOR_ACCOUNTS);
        verify(amendmentService).auditFinaliseStoredProc(
            eq(accountId), eq(RecordType.CREDITOR_ACCOUNTS), eq((short) 77), eq(postedBy), eq(null),
            eq("ACCOUNT_ENQUIRY"));
    }

    @Test
    void updateMinorCreditorAccount_missingPaymentGroup_throwsIllegalArgumentException() {
        // Arrange
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest();

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user")
        );
        assertEquals("Payment group must be provided", ex.getMessage());
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
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(majorAccount));

        // Act + Assert
        assertThrows(
            EntityNotFoundException.class,
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
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(false));

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(minorAccount));
        when(partyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
            EntityNotFoundException.class,
            () -> service.updateMinorCreditorAccount(accountId, request, BigInteger.valueOf(3L), "test.user")
        );
    }
}
