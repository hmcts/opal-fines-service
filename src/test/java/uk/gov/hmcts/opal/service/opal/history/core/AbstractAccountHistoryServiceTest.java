package uk.gov.hmcts.opal.service.opal.history.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbstractAccountHistoryServiceTest {

    @Test
    void getHistory_filtersBySupportedSourcesAndItemTypesAndReturnsSortedItems() {
        AccountHistoryItem amendmentItem = AccountHistoryItem.builder()
            .type(AccountHistoryItemType.AMENDMENT)
            .postedDetails(AccountHistoryPostedDetails.builder().postedBy("a").build())
            .eventDateTime(LocalDateTime.of(2026, 1, 1, 10, 0))
            .sourceId(10L)
            .build();
        AccountHistoryItem noteItem = AccountHistoryItem.builder()
            .type(AccountHistoryItemType.NOTE)
            .postedDetails(AccountHistoryPostedDetails.builder().postedBy("b").build())
            .eventDateTime(LocalDateTime.of(2026, 1, 2, 10, 0))
            .sourceId(11L)
            .build();

        TestSource unsupportedSource = new TestSource(
            false,
            AccountHistoryItemType.AMENDMENT,
            List.of(amendmentItem)
        );
        TestSource filteredOutSource = new TestSource(
            true,
            AccountHistoryItemType.FINANCIAL,
            List.of(AccountHistoryItem.builder().type(AccountHistoryItemType.FINANCIAL).build())
        );
        TestSource includedSource = new TestSource(
            true,
            AccountHistoryItemType.NOTE,
            List.of(noteItem)
        );

        TestAccountHistoryService service = new TestAccountHistoryService(
            List.of(unsupportedSource, filteredOutSource, includedSource)
        );

        AccountHistoryResult result = service.invokeGetHistory(
            262200L,
            AccountHistoryFilter.builder()
                .dateFrom(LocalDate.of(2026, 1, 1))
                .dateTo(LocalDate.of(2026, 1, 31))
                .itemTypes(List.of(AccountHistoryItemType.NOTE))
                .build()
        );

        assertEquals(BigInteger.valueOf(7), result.getVersion());
        assertEquals(1, result.getHistoryItems().size());
        assertSame(noteItem, result.getHistoryItems().get(0));
        assertEquals(0, unsupportedSource.getFetchCount());
        assertEquals(0, filteredOutSource.getFetchCount());
        assertEquals(1, includedSource.getFetchCount());
        assertEquals(262200L, includedSource.getLastContext().getAccountId());
    }

    private static final class TestAccountHistoryService extends AbstractAccountHistoryService {

        private TestAccountHistoryService(List<AccountHistorySource> sources) {
            super(sources);
        }

        private AccountHistoryResult invokeGetHistory(Long accountId, AccountHistoryFilter filter) {
            return getHistory(accountId, filter);
        }

        @Override
        protected AccountHistoryContext buildContext(Long accountId) {
            return new AccountHistoryContext(AccountHistoryType.DEFENDANT, accountId);
        }

        @Override
        protected AccountHistoryContext ensureAccountExists(AccountHistoryContext context) {
            return context.withVersion(BigInteger.valueOf(7));
        }

        @Override
        protected Comparator<AccountHistoryItem> getComparator() {
            return Comparator.comparing(
                AccountHistoryItem::getEventDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
            );
        }
    }

    private static final class TestSource implements AccountHistorySource {

        private final boolean supported;
        private final AccountHistoryItemType itemType;
        private final List<AccountHistoryItem> items;
        private int fetchCount;
        private AccountHistoryContext lastContext;

        private TestSource(boolean supported, AccountHistoryItemType itemType, List<AccountHistoryItem> items) {
            this.supported = supported;
            this.itemType = itemType;
            this.items = items;
        }

        @Override
        public boolean supports(AccountHistoryContext context) {
            return supported;
        }

        @Override
        public AccountHistoryItemType getItemType() {
            return itemType;
        }

        @Override
        public List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter) {
            fetchCount++;
            lastContext = context;
            return items;
        }

        private int getFetchCount() {
            return fetchCount;
        }

        private AccountHistoryContext getLastContext() {
            return lastContext;
        }
    }
}
