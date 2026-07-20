package uk.gov.hmcts.opal.service.opal.history.defendant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItemType;
import uk.gov.hmcts.opal.dto.history.AccountHistoryNoteDetails;
import uk.gov.hmcts.opal.dto.history.AccountHistoryPostedDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.AmendmentHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.DefendantTransactionHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.EnforcementHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.NoteHistorySource;
import uk.gov.hmcts.opal.service.opal.history.defendant.sources.PaymentTermsHistorySource;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountHistoryServiceTest {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private HistoryItemOrderingService historyItemOrderingService;

    @Mock
    private AmendmentHistorySource amendmentSource;

    @Mock
    private EnforcementHistorySource enforcementSource;

    @Mock
    private NoteHistorySource noteSource;

    @Mock
    private PaymentTermsHistorySource paymentTermsSource;

    @Mock
    private DefendantTransactionHistorySource transactionSource;

    @Test
    void getHistory_returnsMappedResponseForIncludedSourcesOnly() {
        DefendantAccountHistoryService service = buildService();
        DefendantAccountEntity defendantAccount = org.mockito.Mockito.mock(DefendantAccountEntity.class);
        when(defendantAccount.getVersion()).thenReturn(BigInteger.valueOf(3));
        when(defendantAccountRepositoryService.findByDefendantAccountId(262200L))
            .thenReturn(Optional.of(defendantAccount));
        when(historyItemOrderingService.newestFirstComparator()).thenReturn(
            Comparator.comparing(AccountHistoryItem::getEventDateTime, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        AccountHistoryItem noteItem = AccountHistoryItem.builder()
            .postedDetails(org.mockito.Mockito.mock(AccountHistoryPostedDetails.class))
            .type(AccountHistoryItemType.NOTE)
            .details(AccountHistoryNoteDetails.builder()
                .noteText("History note")
                .build())
            .eventDateTime(LocalDateTime.of(2026, 1, 4, 9, 0))
            .sourceId(44L)
            .build();

        when(noteSource.supports(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(noteSource.getItemType())
            .thenReturn(AccountHistoryItemType.NOTE);
        when(noteSource.fetch(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .thenReturn(List.of(noteItem));

        when(amendmentSource.supports(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(amendmentSource.getItemType())
            .thenReturn(AccountHistoryItemType.AMENDMENT);
        when(enforcementSource.supports(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(enforcementSource.getItemType())
            .thenReturn(AccountHistoryItemType.ENFORCEMENT);
        when(paymentTermsSource.supports(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(paymentTermsSource.getItemType())
            .thenReturn(AccountHistoryItemType.PAYMENT_TERMS);
        when(transactionSource.supports(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(transactionSource.getItemType())
            .thenReturn(AccountHistoryItemType.FINANCIAL);

        DefendantAccountHistoryResponse response = service.getHistory(
            262200L,
            DefendantAccountHistoryFilter.builder()
                .itemTypes(List.of(HistoryItemType.NOTE))
                .build()
        );

        assertEquals(BigInteger.valueOf(3), response.getVersion());
        assertEquals(1, response.getHistoryItems().size());
        assertEquals(HistoryItemType.NOTE, response.getHistoryItems().get(0).getType());
        assertEquals(NoteDetails.builder().noteText("History note").build(),
            response.getHistoryItems().get(0).getDetails());

        verify(noteSource).fetch(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(amendmentSource, never())
            .fetch(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(enforcementSource, never())
            .fetch(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(paymentTermsSource, never())
            .fetch(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(transactionSource, never())
            .fetch(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getHistory_throwsWhenDefendantAccountIsMissing() {
        DefendantAccountHistoryService service = buildService();
        when(defendantAccountRepositoryService.findByDefendantAccountId(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> service.getHistory(999L, DefendantAccountHistoryFilter.builder().build()));

        assertEquals("Defendant Account not found with id: 999", exception.getMessage());
        verifyNoInteractions(historyItemOrderingService, amendmentSource, enforcementSource, noteSource,
            paymentTermsSource, transactionSource);
    }

    private DefendantAccountHistoryService buildService() {
        return new DefendantAccountHistoryService(
            defendantAccountRepositoryService,
            historyItemOrderingService,
            amendmentSource,
            enforcementSource,
            noteSource,
            paymentTermsSource,
            transactionSource
        );
    }
}
