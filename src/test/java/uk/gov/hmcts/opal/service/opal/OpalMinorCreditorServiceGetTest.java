package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorHistoryItemMapper;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorAmendmentHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorNoteHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;

@ExtendWith(MockitoExtension.class)
class OpalMinorCreditorServiceGetTest {

    @Mock
    private MinorCreditorRepository minorCreditorRepository;

    @Mock
    private MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;

    @Mock
    private MinorCreditorAccountAtAGlanceRepository minorCreditorAccountAtAGlanceRepository;

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private AmendmentRepository amendmentRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CreditorTransactionRepository creditorTransactionRepository;

    @Mock
    private AmendmentService amendmentService;

    @Mock
    private MinorCreditorAccountResponseMapper responseMapper;

    @Spy
    private MinorCreditorHistoryItemMapper historyItemMapper = new MinorCreditorHistoryItemMapper();

    @InjectMocks
    private OpalMinorCreditorService service;

    @Test
    void getMinorCreditorAccount_success_returnsMappedResponseWithVersion() {
        // Arrange
        Long accountId = 101L;
        Long partyId = 201L;

        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .minorCreditorPartyId(partyId)
            .creditorAccountType(CreditorAccountType.MN)
            .versionNumber(5L)
            .build();
        PartyEntity party = PartyEntity.builder().partyId(partyId).organisation(false).build();
        MinorCreditorAccountResponse mappedResponse = new MinorCreditorAccountResponse();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
        when(responseMapper.toMinorCreditorAccountResponse(account, party)).thenReturn(mappedResponse);

        // Act
        MinorCreditorAccountResponse result = service.getMinorCreditorAccount(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(mappedResponse, result);
        assertEquals(BigInteger.valueOf(5L), result.getVersion());
        verify(responseMapper).toMinorCreditorAccountResponse(account, party);
    }

    @Test
    void getMinorCreditorAccount_missingAccount_throwsEntityNotFoundException() {
        // Arrange
        when(creditorAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorAccount(999L));
    }

    @Test
    void getMinorCreditorAccount_nonMinorCreditor_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 101L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MJ)
            .build();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorAccount(accountId));
    }

    @Test
    void getMinorCreditorAccount_missingParty_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 101L;
        Long partyId = 201L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .minorCreditorPartyId(partyId)
            .creditorAccountType(CreditorAccountType.MN)
            .build();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(partyRepository.findById(partyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorAccount(accountId));
    }

    @Test
    void getMinorCreditorHistory_success_returnsHistoryPayloadWithVersion() {
        // Arrange
        Long accountId = 101L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .versionNumber(5L)
            .build();
        LocalDateTime postedFrom = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime postedTo = LocalDateTime.of(2026, 2, 1, 0, 0);
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            postedFrom.toLocalDate(), postedTo.minusDays(1).toLocalDate(), null);

        MinorCreditorAmendmentHistoryProjection amendment = new AmendmentHistoryProjection(
            11L,
            LocalDateTime.of(2026, 1, 31, 10, 0),
            "AMEND",
            "Amend User",
            "Hold Pay Out",
            "false",
            "true");
        MinorCreditorNoteHistoryProjection note = new NoteHistoryProjection(
            12L,
            LocalDateTime.of(2026, 1, 30, 9, 0),
            "NOTE",
            "Note User",
            "Review creditor");
        MinorCreditorTransactionHistoryProjection transaction = new TransactionHistoryProjection(
            13L,
            LocalDateTime.of(2026, 1, 29, 8, 0),
            "PAY",
            "Payment User",
            "PAYMNT",
            BigDecimal.valueOf(42L),
            "PMT001",
            "C",
            LocalDateTime.of(2026, 1, 29, 8, 30),
            "defendant_accounts",
            "70000000000000",
            "HOLD1234",
            "DEF123456",
            70000000000000L);

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(amendmentRepository.findMinorCreditorHistory("101", postedFrom, postedTo))
            .thenReturn(List.of(amendment));
        when(noteRepository.findMinorCreditorHistory("101", postedFrom, postedTo)).thenReturn(List.of(note));
        when(creditorTransactionRepository.findMinorCreditorHistory(accountId, postedFrom, postedTo))
            .thenReturn(List.of(transaction));

        // Act
        GetMinorCreditorHistoryResponse result = service.getMinorCreditorHistory(accountId, filters);

        // Assert
        assertNotNull(result);
        assertEquals(BigInteger.valueOf(5L), result.getVersion());
        assertNotNull(result.getPayload());
        List<MinorCreditorHistoryItemHistory> historyItems = result.getPayload().getHistoryItems();
        assertEquals(3, historyItems.size());
        assertEquals(MinorCreditorHistoryItemHistory.TypeEnum.AMENDMENT, historyItems.get(0).getType());
        assertEquals("2026-01-31", historyItems.get(0).getPostedDetails().getPostedDate().toString());
        assertEquals("Hold Pay Out", ((AmendmentTypeCommon) historyItems.get(0).getDetails()).getAttributeName());
        assertEquals(MinorCreditorHistoryItemHistory.TypeEnum.NOTE, historyItems.get(1).getType());
        assertEquals("Review creditor", ((NoteDetailsHistory) historyItems.get(1).getDetails()).getNoteText());
        assertEquals(MinorCreditorHistoryItemHistory.TypeEnum.FINANCIAL, historyItems.get(2).getType());
        CreditorTransactionDetailsHistory financialDetails =
            (CreditorTransactionDetailsHistory) historyItems.get(2).getDetails();
        assertEquals(BigDecimal.valueOf(42L), historyItems.get(2).getAmount());
        assertEquals("PAYMNT", financialDetails.getTransactionType().getTransactionType().getValue());
        assertEquals(70000000000000L, financialDetails.getDefendantAccountId());
        verify(amendmentRepository).findMinorCreditorHistory("101", postedFrom, postedTo);
        verify(noteRepository).findMinorCreditorHistory("101", postedFrom, postedTo);
        verify(creditorTransactionRepository).findMinorCreditorHistory(accountId, postedFrom, postedTo);
    }

    @Test
    void getMinorCreditorHistory_missingAccount_throwsEntityNotFoundException() {
        // Arrange
        when(creditorAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorHistory(
            999L, MinorCreditorHistoryFilters.from(null, null, null)));
    }

    @Test
    void getMinorCreditorHistory_nonMinorCreditor_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 101L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MJ)
            .build();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorHistory(
            accountId, MinorCreditorHistoryFilters.from(null, null, null)));
    }

    private record AmendmentHistoryProjection(
        Long amendmentId,
        LocalDateTime postedDate,
        String postedBy,
        String postedByName,
        String attributeName,
        String oldValue,
        String newValue) implements MinorCreditorAmendmentHistoryProjection {

        @Override
        public Long getAmendmentId() {
            return amendmentId;
        }

        @Override
        public LocalDateTime getPostedDate() {
            return postedDate;
        }

        @Override
        public String getPostedBy() {
            return postedBy;
        }

        @Override
        public String getPostedByName() {
            return postedByName;
        }

        @Override
        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public String getOldValue() {
            return oldValue;
        }

        @Override
        public String getNewValue() {
            return newValue;
        }
    }

    private record NoteHistoryProjection(
        Long noteId,
        LocalDateTime postedDate,
        String postedBy,
        String postedByName,
        String noteText) implements MinorCreditorNoteHistoryProjection {

        @Override
        public Long getNoteId() {
            return noteId;
        }

        @Override
        public LocalDateTime getPostedDate() {
            return postedDate;
        }

        @Override
        public String getPostedBy() {
            return postedBy;
        }

        @Override
        public String getPostedByName() {
            return postedByName;
        }

        @Override
        public String getNoteText() {
            return noteText;
        }
    }

    private record TransactionHistoryProjection(
        Long creditorTransactionId,
        LocalDateTime postedDate,
        String postedBy,
        String postedByName,
        String transactionType,
        BigDecimal transactionAmount,
        String paymentReference,
        String status,
        LocalDateTime statusDate,
        String associatedRecordType,
        String associatedRecordId,
        String accountNumber,
        String defendantAccountNumber,
        Long defendantAccountId) implements MinorCreditorTransactionHistoryProjection {

        @Override
        public Long getCreditorTransactionId() {
            return creditorTransactionId;
        }

        @Override
        public LocalDateTime getPostedDate() {
            return postedDate;
        }

        @Override
        public String getPostedBy() {
            return postedBy;
        }

        @Override
        public String getPostedByName() {
            return postedByName;
        }

        @Override
        public String getTransactionType() {
            return transactionType;
        }

        @Override
        public BigDecimal getTransactionAmount() {
            return transactionAmount;
        }

        @Override
        public String getPaymentReference() {
            return paymentReference;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public LocalDateTime getStatusDate() {
            return statusDate;
        }

        @Override
        public String getAssociatedRecordType() {
            return associatedRecordType;
        }

        @Override
        public String getAssociatedRecordId() {
            return associatedRecordId;
        }

        @Override
        public String getAccountNumber() {
            return accountNumber;
        }

        @Override
        public String getDefendantAccountNumber() {
            return defendantAccountNumber;
        }

        @Override
        public Long getDefendantAccountId() {
            return defendantAccountId;
        }
    }
}
