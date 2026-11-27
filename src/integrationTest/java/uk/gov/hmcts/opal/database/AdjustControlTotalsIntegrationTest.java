package uk.gov.hmcts.opal.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Adjust control totals database integration tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_adjust_control_totals_integration_test.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_adjust_control_totals_integration_test.sql",
    executionPhase = AFTER_TEST_CLASS
)
@Transactional
class AdjustControlTotalsIntegrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 32760;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @JiraStory("PO-3461")
    @JiraEpic("PO-2353")
    @DisplayName("Creates and updates CT, WOT and counter totals together and independently")
    void adjustsCtWotAndCounterTogetherAndIndependently() {
        adjust(items(item("CT", 21, "100.00"), item("WOT", 22, "100.00")),
            (short) 23, amount("1.00"), "suspense_transactions", "4001");

        adjust(items(item("CT", 21, "10.00"), item("WOT", 22, "20.00")),
            (short) 23, amount("2.00"), "suspense_transactions", "4001");

        adjust(items(item("CT", 21, "1.00")), null, null, "suspense_transactions", "4001");
        adjust(items(item("WOT", 22, "2.00")), null, null, "suspense_transactions", "4001");
        adjust(null, (short) 23, amount("3.00"), "suspense_transactions", "4001");

        assertThat(controlTotals())
            .extracting(ControlTotal::itemNumber, ControlTotal::amount,
                ControlTotal::associatedRecordType, ControlTotal::associatedRecordId)
            .containsExactlyInAnyOrder(
                tuple((short) 21, amount("111.00"), "suspense_transactions", "4001"),
                tuple((short) 22, amount("122.00"), "suspense_transactions", "4001"),
                tuple((short) 23, amount("6.00"), "suspense_transactions", "4001"));
    }

    @Test
    @JiraStory("PO-3461")
    @JiraEpic("PO-2353")
    @DisplayName("Processes a counter when the item array is empty or NULL")
    void processesCounterWithEmptyAndNullItemArrays() {
        adjust(List.of(), (short) 30, amount("1.00"), null, null);
        adjust(null, (short) 30, amount("2.00"), null, null);

        assertThat(controlTotals())
            .extracting(ControlTotal::itemNumber, ControlTotal::amount)
            .containsExactlyInAnyOrder(tuple((short) 30, amount("3.00")));
    }

    @Test
    @JiraStory("PO-3461")
    @JiraEpic("PO-2353")
    @DisplayName("Leaves existing totals unchanged when there are no inputs")
    void leavesExistingTotalsUnchangedWhenThereAreNoInputs() {
        adjust(items(item("CT", 31, "100.25"), item("WOT", 32, "100.25")),
            (short) 33, amount("1.00"), "defendant_accounts", "5001");
        List<ControlTotal> before = controlTotals();

        adjustWithDefaults(null, null, null);

        assertThat(controlTotals()).containsExactlyInAnyOrderElementsOf(before);
    }

    @Test
    @JiraStory("PO-10795")
    @JiraEpic("PO-2997")
    @DisplayName("Updates only the record with the matching associated record ID")
    void updatesOnlyMatchingAssociatedRecordId() {
        adjust(items(item("CT", 40, "10.00")), null, null, "suspense_transactions", "1001");
        adjust(items(item("CT", 40, "5.00")), null, null, "suspense_transactions", "1002");
        adjust(items(item("CT", 40, "3.00")), null, null, "suspense_transactions", "1001");

        assertThat(controlTotals())
            .extracting(ControlTotal::amount, ControlTotal::associatedRecordType, ControlTotal::associatedRecordId)
            .containsExactlyInAnyOrder(
                tuple(amount("13.00"), "suspense_transactions", "1001"),
                tuple(amount("5.00"), "suspense_transactions", "1002"));
    }

    @Test
    @JiraStory("PO-10795")
    @JiraEpic("PO-2997")
    @DisplayName("Updates only the record with the matching associated record type")
    void updatesOnlyMatchingAssociatedRecordType() {
        adjust(items(item("CT", 41, "10.00")), null, null, "suspense_transactions", "2001");
        adjust(items(item("CT", 41, "5.00")), null, null, "defendant_accounts", "2001");
        adjust(items(item("CT", 41, "3.00")), null, null, "suspense_transactions", "2001");

        assertThat(controlTotals())
            .extracting(ControlTotal::amount, ControlTotal::associatedRecordType, ControlTotal::associatedRecordId)
            .containsExactlyInAnyOrder(
                tuple(amount("5.00"), "defendant_accounts", "2001"),
                tuple(amount("13.00"), "suspense_transactions", "2001"));
    }

    @Test
    @JiraStory("PO-10795")
    @JiraEpic("PO-2997")
    @DisplayName("Associates an existing CT total when association parameters are supplied")
    void associatesExistingTotalWhenAssociationParametersAreSupplied() {
        adjustWithDefaults(items(item("CT", 43, "10.00")), null, null);
        adjust(items(item("CT", 43, "5.00")), null, null, "suspense_transactions", "4002");

        assertThat(controlTotals())
            .extracting(ControlTotal::amount, ControlTotal::associatedRecordType, ControlTotal::associatedRecordId)
            .containsExactly(tuple(amount("15.00"), "suspense_transactions", "4002"));
    }

    @Test
    @JiraStory("PO-10795")
    @JiraEpic("PO-2997")
    @DisplayName("Optional association parameters restrict matching only when supplied")
    void optionalAssociationParametersRestrictMatchingOnlyWhenSupplied() {
        adjust(items(item("CT", 42, "10.00")), null, null, "suspense_transactions", "3001");
        adjust(items(item("CT", 42, "5.00")), null, null, "defendant_accounts", "3002");

        // Type supplied, ID omitted: match the type and preserve the existing ID.
        adjust(items(item("CT", 42, "1.00")), null, null, "defendant_accounts", null);
        // ID supplied, type omitted: match the ID and preserve the existing type.
        adjust(items(item("CT", 42, "2.00")), null, null, null, "3001");
        // Both omitted: update all rows for the item without clearing either association.
        adjustWithDefaults(items(item("CT", 42, "1.00")), null, null);

        assertThat(controlTotals())
            .extracting(ControlTotal::amount, ControlTotal::associatedRecordType, ControlTotal::associatedRecordId)
            .containsExactlyInAnyOrder(
                tuple(amount("7.00"), "defendant_accounts", "3002"),
                tuple(amount("13.00"), "suspense_transactions", "3001"));
    }

    @Test
    @JiraStory("PO-10795")
    @JiraEpic("PO-2997")
    @DisplayName("Updates three associated totals without duplicating rows when association parameters are omitted")
    void updatesThreeTotalsAndPreservesAssociationsWhenAssociationParametersAreOmitted() {
        adjust(items(item("CT", 51, "100.25"), item("WOT", 52, "100.25")),
            (short) 53, amount("1.00"), "suspense_transactions", "6001");

        adjustWithDefaults(items(item("CT", 51, "25.50"), item("WOT", 52, "25.50")),
            (short) 53, amount("1.00"));

        assertThat(controlTotals())
            .extracting(ControlTotal::itemNumber, ControlTotal::amount,
                ControlTotal::associatedRecordType, ControlTotal::associatedRecordId)
            .containsExactlyInAnyOrder(
                tuple((short) 51, amount("125.75"), "suspense_transactions", "6001"),
                tuple((short) 52, amount("125.75"), "suspense_transactions", "6001"),
                tuple((short) 53, amount("2.00"), "suspense_transactions", "6001"));
    }

    private void adjust(List<ItemAmount> items, Short counterItemNumber, BigDecimal accountCount,
                        String associatedRecordType, String associatedRecordId) {
        executeProcedure(items, counterItemNumber, accountCount, associatedRecordType, associatedRecordId, true);
    }

    private void adjustWithDefaults(List<ItemAmount> items, Short counterItemNumber, BigDecimal accountCount) {
        executeProcedure(items, counterItemNumber, accountCount, null, null, false);
    }

    private void executeProcedure(List<ItemAmount> items, Short counterItemNumber, BigDecimal accountCount,
                                  String associatedRecordType, String associatedRecordId,
                                  boolean includeAssociationParameters) {
        entityManager.flush();
        entityManager.clear();

        String associationParameters = includeAssociationParameters
            ? ", CAST(? AS public.t_associated_record_type_enum), ?" : "";
        String procedureCall = "CALL public.p_adjust_control_totals(?, " + itemArraySql(items)
            + ", ?, ?" + associationParameters + ")";
        Query query = entityManager.createNativeQuery(procedureCall);
        int parameterIndex = 1;
        query.setParameter(parameterIndex++, BUSINESS_UNIT_ID);
        if (items != null && !items.isEmpty()) {
            for (ItemAmount item : items) {
                query.setParameter(parameterIndex++, item.controlTotalType());
                query.setParameter(parameterIndex++, item.itemNumber());
                query.setParameter(parameterIndex++, item.amount());
            }
        }
        query.setParameter(parameterIndex++, counterItemNumber);
        query.setParameter(parameterIndex++, accountCount);
        if (includeAssociationParameters) {
            query.setParameter(parameterIndex++, associatedRecordType);
            query.setParameter(parameterIndex, associatedRecordId);
        }
        query.executeUpdate();
        entityManager.clear();
    }

    private String itemArraySql(List<ItemAmount> items) {
        if (items == null) {
            return "CAST(NULL AS control_total_item_amount_type[])";
        }
        if (items.isEmpty()) {
            return "ARRAY[]::control_total_item_amount_type[]";
        }
        return "ARRAY[" + String.join(", ", items.stream()
            .map(item -> "ROW(?, ?, ?)::public.control_total_item_amount_type")
            .toList()) + "]";
    }

    private List<ControlTotal> controlTotals() {
        entityManager.flush();
        entityManager.clear();
        return entityManager.createQuery("""
                SELECT controlTotal
                FROM ControlTotalEntity controlTotal
                WHERE controlTotal.businessUnit.businessUnitId = :businessUnitId
                """, ControlTotalEntity.class)
            .setParameter("businessUnitId", BUSINESS_UNIT_ID)
            .getResultList()
            .stream()
            .map(controlTotal -> new ControlTotal(
                controlTotal.getItemNumber(),
                controlTotal.getAmount(),
                controlTotal.getAssociatedRecordType() == null
                    ? null : controlTotal.getAssociatedRecordType().getLabel(),
                controlTotal.getAssociatedRecordId()
            ))
            .toList();
    }

    private List<ItemAmount> items(ItemAmount... items) {
        return List.of(items);
    }

    private ItemAmount item(String type, int itemNumber, String amount) {
        return new ItemAmount(type, (short) itemNumber, amount(amount));
    }

    private BigDecimal amount(String amount) {
        return new BigDecimal(amount);
    }

    private record ItemAmount(String controlTotalType, Short itemNumber, BigDecimal amount) {
    }

    private record ControlTotal(Short itemNumber, BigDecimal amount, String associatedRecordType,
                                String associatedRecordId) {
    }
}
