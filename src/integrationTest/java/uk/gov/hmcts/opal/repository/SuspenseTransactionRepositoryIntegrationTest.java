package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Suspense transaction decimal amount persistence")
@Sql(scripts = "classpath:db/insertData/insert_into_suspense_item_summary.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_suspense_item_summary.sql", executionPhase = AFTER_TEST_CLASS)
class SuspenseTransactionRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SuspenseTransactionRepository repository;

    @ParameterizedTest
    @CsvSource({"99000000979601, 100.50", "99000000979602, -25.25"})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Loading an entity preserves positive and negative monetary amounts with their decimal places")
    void findById_preservesDecimalAmount(long transactionId, String expectedAmount) {
        SuspenseTransactionEntity transaction = repository.findById(transactionId).orElseThrow();

        assertEquals(new BigDecimal(expectedAmount), transaction.getAmount());
    }
}
