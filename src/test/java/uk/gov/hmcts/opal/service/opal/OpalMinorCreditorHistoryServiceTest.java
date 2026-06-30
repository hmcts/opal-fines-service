package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.AMENDMENT;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.FINANCIAL;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.NOTE;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.mapper.MinorCreditorHistoryItemMapper;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorAmendmentHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorNoteHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;

@ExtendWith(MockitoExtension.class)
class OpalMinorCreditorHistoryServiceTest {

    private static final Long ACCOUNT_ID = 101L;
    private static final LocalDateTime MIN_POSTED_DATE = LocalDateTime.of(1, 1, 1, 0, 0);
    private static final LocalDateTime MAX_POSTED_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private AmendmentRepository amendmentRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CreditorTransactionRepository creditorTransactionRepository;

    @Spy
    private MinorCreditorHistoryItemMapper historyItemMapper = new MinorCreditorHistoryItemMapper();

    @InjectMocks
    private OpalMinorCreditorService service;

    @Test
    void getMinorCreditorHistory_missingAccount_throwsEntityNotFoundException() {
        // Arrange
        when(creditorAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, null)));
        verifyNoInteractions(amendmentRepository, noteRepository, creditorTransactionRepository);
    }

    @Test
    void getMinorCreditorHistory_nonMinorCreditor_throwsEntityNotFoundException() {
        // Arrange
        when(creditorAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(
            CreditorAccountEntity.builder()
                .creditorAccountId(ACCOUNT_ID)
                .creditorAccountType(CreditorAccountType.MJ)
                .build()));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, null)));
        verifyNoInteractions(amendmentRepository, noteRepository, creditorTransactionRepository);
    }

    @Test
    void getMinorCreditorHistory_withoutFilters_queriesAllSourcesWithOpenRange() {
        // Arrange
        givenMinorCreditorAccount();
        when(amendmentRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of());
        when(noteRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of());
        when(creditorTransactionRepository.findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of());

        // Act
        GetMinorCreditorHistoryResponse result = service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, null));

        // Assert
        assertThat(result.getVersion()).isEqualTo(BigInteger.valueOf(7L));
        assertThat(result.getPayload().getHistoryItems()).isEmpty();
        verify(amendmentRepository).findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE);
        verify(noteRepository).findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE);
        verify(creditorTransactionRepository).findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE);
    }

    @Test
    void getMinorCreditorHistory_withNoteFilter_queriesOnlyNotes() {
        // Arrange
        givenMinorCreditorAccount();
        when(noteRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of());

        // Act
        service.getMinorCreditorHistory(ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, List.of("note")));

        // Assert
        verify(noteRepository).findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE);
        verifyNoInteractions(amendmentRepository, creditorTransactionRepository);
    }

    @Test
    void getMinorCreditorHistory_withMultipleItemTypes_queriesRequestedSources() {
        // Arrange
        givenMinorCreditorAccount();
        when(amendmentRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of());
        when(creditorTransactionRepository.findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of());

        // Act
        service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, List.of("amendment,financial")));

        // Assert
        verify(amendmentRepository).findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE);
        verify(creditorTransactionRepository).findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE);
        verifyNoInteractions(noteRepository);
    }

    @Test
    void getMinorCreditorHistory_withDateFrom_appliesInclusiveLowerBound() {
        // Arrange
        LocalDate dateFrom = LocalDate.of(2026, 1, 29);
        LocalDateTime postedFrom = LocalDateTime.of(2026, 1, 29, 0, 0);
        givenMinorCreditorAccount();
        when(noteRepository.findMinorCreditorHistory("101", postedFrom, MAX_POSTED_DATE))
            .thenReturn(List.of());

        // Act
        service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(dateFrom, null, List.of("note")));

        // Assert
        verify(noteRepository).findMinorCreditorHistory("101", postedFrom, MAX_POSTED_DATE);
        verifyNoInteractions(amendmentRepository, creditorTransactionRepository);
    }

    @Test
    void getMinorCreditorHistory_withDateTo_appliesInclusiveUpperBound() {
        // Arrange
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        LocalDateTime postedToExclusive = LocalDateTime.of(2026, 2, 1, 0, 0);
        givenMinorCreditorAccount();
        when(noteRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, postedToExclusive))
            .thenReturn(List.of());

        // Act
        service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, dateTo, List.of("note")));

        // Assert
        verify(noteRepository).findMinorCreditorHistory("101", MIN_POSTED_DATE, postedToExclusive);
        verifyNoInteractions(amendmentRepository, creditorTransactionRepository);
    }

    @Test
    void getMinorCreditorHistory_withDateRangeAndFinancialFilter_appliesCombinedFilters() {
        // Arrange
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        LocalDateTime postedFrom = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime postedToExclusive = LocalDateTime.of(2026, 2, 1, 0, 0);
        givenMinorCreditorAccount();
        when(creditorTransactionRepository.findMinorCreditorHistory(
            ACCOUNT_ID, postedFrom, postedToExclusive)).thenReturn(List.of());

        // Act
        service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(dateFrom, dateTo, List.of("financial")));

        // Assert
        verify(creditorTransactionRepository).findMinorCreditorHistory(ACCOUNT_ID, postedFrom, postedToExclusive);
        verifyNoInteractions(amendmentRepository, noteRepository);
    }

    @Test
    void getMinorCreditorHistory_ordersMergedItemsLatestToOldest() {
        // Arrange
        LocalDateTime oldest = LocalDateTime.of(2026, 1, 29, 8, 0);
        LocalDateTime middle = LocalDateTime.of(2026, 1, 30, 9, 0);
        LocalDateTime latest = LocalDateTime.of(2026, 1, 31, 10, 0);
        MinorCreditorAmendmentHistoryProjection amendment = amendment(11L, oldest, "Old amendment");
        MinorCreditorNoteHistoryProjection note = note(12L, latest, "Latest note");
        final MinorCreditorTransactionHistoryProjection transaction =
            transaction(13L, middle, "PAYMNT", BigDecimal.valueOf(42L));
        givenMinorCreditorAccount();
        when(amendmentRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(amendment));
        when(noteRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(note));
        when(creditorTransactionRepository.findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(transaction));

        // Act
        List<MinorCreditorHistoryItemHistory> historyItems = service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, null)).getPayload().getHistoryItems();

        // Assert
        assertThat(historyItems).extracting(MinorCreditorHistoryItemHistory::getType).containsExactly(
            NOTE.responseType(),
            FINANCIAL.responseType(),
            AMENDMENT.responseType()
        );
    }

    @Test
    void getMinorCreditorHistory_ordersEqualTimestampsByTypeThenSourceId() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 31, 10, 0);
        MinorCreditorAmendmentHistoryProjection amendment20 = amendment(20L, timestamp, "Amendment 20");
        MinorCreditorAmendmentHistoryProjection amendment10 = amendment(10L, timestamp, "Amendment 10");
        MinorCreditorNoteHistoryProjection note = note(30L, timestamp, "Note 30");
        final MinorCreditorTransactionHistoryProjection transaction =
            transaction(40L, timestamp, "PAYMNT", BigDecimal.valueOf(42L));
        givenMinorCreditorAccount();
        when(amendmentRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(amendment20, amendment10));
        when(noteRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(note));
        when(creditorTransactionRepository.findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(transaction));

        // Act
        List<MinorCreditorHistoryItemHistory> historyItems = service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, null)).getPayload().getHistoryItems();

        // Assert
        assertThat(historyItems).extracting(MinorCreditorHistoryItemHistory::getType).containsExactly(
            AMENDMENT.responseType(),
            AMENDMENT.responseType(),
            FINANCIAL.responseType(),
            NOTE.responseType()
        );
        assertThat(((AmendmentTypeCommon) historyItems.get(0).getDetails()).getAttributeName())
            .isEqualTo("Amendment 10");
        assertThat(((AmendmentTypeCommon) historyItems.get(1).getDetails()).getAttributeName())
            .isEqualTo("Amendment 20");
    }

    @Test
    void getMinorCreditorHistory_mapsDetailsForAllSourceTypes() {
        // Arrange
        LocalDateTime amendmentDate = LocalDateTime.of(2026, 1, 31, 10, 0);
        LocalDateTime noteDate = LocalDateTime.of(2026, 1, 30, 9, 0);
        LocalDateTime transactionDate = LocalDateTime.of(2026, 1, 29, 8, 0);
        MinorCreditorAmendmentHistoryProjection amendment = amendment(11L, amendmentDate, "Hold Pay Out");
        MinorCreditorNoteHistoryProjection note = note(12L, noteDate, "Review creditor");
        final MinorCreditorTransactionHistoryProjection transaction =
            transaction(13L, transactionDate, "PAYMNT", BigDecimal.valueOf(42L));
        givenMinorCreditorAccount();
        when(amendmentRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(amendment));
        when(noteRepository.findMinorCreditorHistory("101", MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(note));
        when(creditorTransactionRepository.findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(transaction));

        // Act
        List<MinorCreditorHistoryItemHistory> historyItems = service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, null)).getPayload().getHistoryItems();

        // Assert
        AmendmentTypeCommon amendmentDetails = (AmendmentTypeCommon) historyItems.get(0).getDetails();
        assertThat(amendmentDetails.getAttributeName()).isEqualTo("Hold Pay Out");
        assertThat(amendmentDetails.getOldValue()).isEqualTo("old-Hold Pay Out");
        assertThat(amendmentDetails.getNewValue()).isEqualTo("new-Hold Pay Out");

        NoteDetailsHistory noteDetails = (NoteDetailsHistory) historyItems.get(1).getDetails();
        assertThat(noteDetails.getNoteText()).isEqualTo("Review creditor");

        CreditorTransactionDetailsHistory transactionDetails =
            (CreditorTransactionDetailsHistory) historyItems.get(2).getDetails();
        assertThat(historyItems.get(2).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(42L));
        assertThat(transactionDetails.getTransactionType().getTransactionType().getValue()).isEqualTo("PAYMNT");
        assertThat(transactionDetails.getPaymentReference()).isEqualTo("PMT001");
        assertThat(transactionDetails.getStatus().getCreditorTransactionStatus().getValue()).isEqualTo("C");
        assertThat(transactionDetails.getAccountNumber()).isEqualTo("HOLD1234");
        assertThat(transactionDetails.getDefendantAccountNumber()).isEqualTo("DEF123456");
        assertThat(transactionDetails.getDefendantAccountId()).isEqualTo(70000000000000L);
    }

    @Test
    void getMinorCreditorHistory_mapsNullTransactionOptionals() {
        // Arrange
        LocalDateTime transactionDate = LocalDateTime.of(2026, 1, 29, 8, 0);
        MinorCreditorTransactionHistoryProjection transaction = transaction(
            13L, transactionDate, "PAYMNT", BigDecimal.valueOf(42L));
        when(transaction.getPaymentReference()).thenReturn(null);
        when(transaction.getStatus()).thenReturn(null);
        when(transaction.getStatusDate()).thenReturn(null);
        when(transaction.getAssociatedRecordType()).thenReturn(null);
        when(transaction.getAssociatedRecordId()).thenReturn(null);
        when(transaction.getAccountNumber()).thenReturn(null);
        when(transaction.getDefendantAccountNumber()).thenReturn(null);
        when(transaction.getDefendantAccountId()).thenReturn(null);
        givenMinorCreditorAccount();
        when(creditorTransactionRepository.findMinorCreditorHistory(ACCOUNT_ID, MIN_POSTED_DATE, MAX_POSTED_DATE))
            .thenReturn(List.of(transaction));

        // Act
        List<MinorCreditorHistoryItemHistory> historyItems = service.getMinorCreditorHistory(
            ACCOUNT_ID, MinorCreditorHistoryFilters.from(null, null, List.of("financial")))
            .getPayload().getHistoryItems();

        // Assert
        CreditorTransactionDetailsHistory details =
            (CreditorTransactionDetailsHistory) historyItems.getFirst().getDetails();
        assertThat(details.getPaymentReference()).isNull();
        assertThat(details.getStatus()).isNull();
        assertThat(details.getStatusDate()).isNull();
        assertThat(details.getAssociatedRecordType()).isNull();
        assertThat(details.getAssociatedRecordId()).isNull();
        assertThat(details.getAccountNumber()).isNull();
        assertThat(details.getDefendantAccountNumber()).isNull();
        assertThat(details.getDefendantAccountId()).isNull();
    }

    private void givenMinorCreditorAccount() {
        when(creditorAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(
            CreditorAccountEntity.builder()
                .creditorAccountId(ACCOUNT_ID)
                .creditorAccountType(CreditorAccountType.MN)
                .versionNumber(7L)
                .build()));
    }

    private MinorCreditorAmendmentHistoryProjection amendment(
        Long amendmentId,
        LocalDateTime postedDate,
        String attributeName) {
        MinorCreditorAmendmentHistoryProjection projection =
            mock(MinorCreditorAmendmentHistoryProjection.class);
        when(projection.getAmendmentId()).thenReturn(amendmentId);
        when(projection.getPostedDate()).thenReturn(postedDate);
        when(projection.getPostedBy()).thenReturn("AMENDUSR");
        when(projection.getPostedByName()).thenReturn("Amend User");
        when(projection.getAttributeName()).thenReturn(attributeName);
        when(projection.getOldValue()).thenReturn("old-" + attributeName);
        when(projection.getNewValue()).thenReturn("new-" + attributeName);
        return projection;
    }

    private MinorCreditorNoteHistoryProjection note(Long noteId, LocalDateTime postedDate, String noteText) {
        MinorCreditorNoteHistoryProjection projection = mock(MinorCreditorNoteHistoryProjection.class);
        when(projection.getNoteId()).thenReturn(noteId);
        when(projection.getPostedDate()).thenReturn(postedDate);
        when(projection.getPostedBy()).thenReturn("NOTEUSR");
        when(projection.getPostedByName()).thenReturn("Note User");
        when(projection.getNoteText()).thenReturn(noteText);
        return projection;
    }

    private MinorCreditorTransactionHistoryProjection transaction(
        Long transactionId,
        LocalDateTime postedDate,
        String transactionType,
        BigDecimal amount) {
        MinorCreditorTransactionHistoryProjection projection =
            mock(MinorCreditorTransactionHistoryProjection.class);
        when(projection.getCreditorTransactionId()).thenReturn(transactionId);
        when(projection.getPostedDate()).thenReturn(postedDate);
        when(projection.getPostedBy()).thenReturn("PAYUSR");
        when(projection.getPostedByName()).thenReturn("Payment User");
        when(projection.getTransactionType()).thenReturn(transactionType);
        when(projection.getTransactionAmount()).thenReturn(amount);
        when(projection.getPaymentReference()).thenReturn("PMT001");
        when(projection.getStatus()).thenReturn("C");
        when(projection.getStatusDate()).thenReturn(postedDate.plusMinutes(30));
        when(projection.getAssociatedRecordType()).thenReturn("defendant_accounts");
        when(projection.getAssociatedRecordId()).thenReturn("70000000000000");
        when(projection.getAccountNumber()).thenReturn("HOLD1234");
        when(projection.getDefendantAccountNumber()).thenReturn("DEF123456");
        when(projection.getDefendantAccountId()).thenReturn(70000000000000L);
        return projection;
    }
}
