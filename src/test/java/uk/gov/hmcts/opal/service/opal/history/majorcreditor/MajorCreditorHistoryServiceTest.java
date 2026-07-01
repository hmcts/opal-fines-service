package uk.gov.hmcts.opal.service.opal.history.majorcreditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.history.AccountHistoryContext;
import uk.gov.hmcts.opal.dto.history.AccountHistoryCreditorTransactionDetails;
import uk.gov.hmcts.opal.dto.history.AccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItemType;
import uk.gov.hmcts.opal.dto.history.AccountHistoryPostedDetails;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;
import uk.gov.hmcts.opal.service.opal.history.majorcreditor.sources.MajorCreditorTransactionHistorySource;

@ExtendWith(MockitoExtension.class)
class MajorCreditorHistoryServiceTest {

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private MajorCreditorTransactionHistorySource transactionSource;

    private MajorCreditorHistoryService service;

    @BeforeEach
    void setUp() {
        service = new MajorCreditorHistoryService(
            creditorAccountRepository,
            new HistoryItemOrderingService(),
            transactionSource
        );
    }

    @Test
    void getHistory_usesAbstractHistoryFlowAndMapsGeneratedResponse() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        AccountHistoryItem historyItem = AccountHistoryItem.builder()
            .postedDetails(AccountHistoryPostedDetails.builder()
                .postedDate(LocalDateTime.of(2026, 1, 2, 10, 15))
                .postedBy("user1")
                .postedByName("User One")
                .build())
            .type(AccountHistoryItemType.FINANCIAL)
            .details(AccountHistoryCreditorTransactionDetails.builder()
                .transactionType("PAYMNT")
                .paymentReference("PAY123")
                .status("C")
                .statusDate(LocalDateTime.of(2026, 1, 3, 9, 0))
                .accountNumber("MC123")
                .defendantAccountNumber("DA123")
                .defendantAccountId(44L)
                .build())
            .amount(BigDecimal.TEN)
            .eventDateTime(LocalDateTime.of(2026, 1, 2, 10, 15))
            .sourceId(99L)
            .build();

        when(creditorAccountRepository.findById(123L)).thenReturn(Optional.of(CreditorAccountEntity.builder()
            .creditorAccountId(123L)
            .creditorAccountType(CreditorAccountType.MJ)
            .versionNumber(7L)
            .build()));
        when(transactionSource.supports(any(AccountHistoryContext.class))).thenReturn(true);
        when(transactionSource.getItemType()).thenReturn(AccountHistoryItemType.FINANCIAL);
        when(transactionSource.fetch(any(AccountHistoryContext.class), any(AccountHistoryFilter.class)))
            .thenReturn(List.of(historyItem));

        var response = service.getHistory(123L, dateFrom, dateTo, List.of("financial", "note"));

        assertEquals("\"7\"", uk.gov.hmcts.opal.util.VersionUtils.createETag(response));
        assertEquals(1, response.getPayload().getHistoryItems().size());
        MajorCreditorHistoryItemHistory generatedItem = response.getPayload().getHistoryItems().get(0);
        assertEquals(MajorCreditorHistoryItemHistory.TypeEnum.FINANCIAL, generatedItem.getType());
        assertEquals(LocalDate.of(2026, 1, 2), generatedItem.getPostedDetails().getPostedDate());
        assertEquals(BigDecimal.TEN, generatedItem.getAmount());
        assertEquals("PAY123", generatedItem.getDetails().getPaymentReference());
        assertEquals("MC123", generatedItem.getDetails().getAccountNumber());
        assertEquals("DA123", generatedItem.getDetails().getDefendantAccountNumber());
        assertEquals(44L, generatedItem.getDetails().getDefendantAccountId());

        ArgumentCaptor<AccountHistoryFilter> filterCaptor = ArgumentCaptor.forClass(AccountHistoryFilter.class);
        verify(transactionSource).fetch(any(AccountHistoryContext.class), filterCaptor.capture());
        assertEquals(dateFrom, filterCaptor.getValue().getDateFrom());
        assertEquals(dateTo, filterCaptor.getValue().getDateTo());
        assertEquals(List.of(AccountHistoryItemType.FINANCIAL, AccountHistoryItemType.NOTE),
            filterCaptor.getValue().getItemTypes());
    }

    @Test
    void getHistory_throwsWhenAccountIsNotMajorCreditor() {
        when(creditorAccountRepository.findById(123L)).thenReturn(Optional.of(CreditorAccountEntity.builder()
            .creditorAccountId(123L)
            .creditorAccountType(CreditorAccountType.MN)
            .build()));

        assertThrows(EntityNotFoundException.class, () -> service.getHistory(123L, null, null, null));
    }
}
