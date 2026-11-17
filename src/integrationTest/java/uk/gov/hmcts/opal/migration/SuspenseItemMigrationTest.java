package uk.gov.hmcts.opal.migration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Suspense item migration integration tests")
class SuspenseItemMigrationTest extends AbstractIntegrationTest {

    @Test
    @JiraStory("PO-10344")
    @JiraEpic("PO-9048")
    @DisplayName("PO-10344: summary view exposes suspense item version_number as BIGINT")
    void shouldExposeVersionNumberInSuspenseItemSummaryView() {
        var columns = jdbcTemplate.queryForList("""
            SELECT data_type
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'v_suspense_item_summary'
              AND column_name = 'version_number'
            """);

        assertEquals(1, columns.size(), "public.v_suspense_item_summary.version_number must exist");
        assertEquals("bigint", columns.getFirst().get("data_type"), "version_number must be BIGINT");

        var viewDefinition = jdbcTemplate.queryForObject(
            "SELECT pg_get_viewdef('public.v_suspense_item_summary'::regclass, true)",
            String.class
        );
        assertTrue(
            viewDefinition != null && viewDefinition.contains("si.version_number"),
            "The summary view must fetch version_number from suspense_items"
        );
    }

    @Test
    @JiraStory("PO-10343")
    @JiraEpic("PO-9048")
    @DisplayName("PO-10343: version_number exists as a nullable BIGINT after migration")
    void shouldAddNullableBigintVersionNumber() {
        var columns = jdbcTemplate.queryForList("""
            SELECT data_type, is_nullable
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'suspense_items'
              AND column_name = 'version_number'
            """);

        assertEquals(1, columns.size(), "public.suspense_items.version_number must exist");

        var column = columns.getFirst();
        assertAll(
            () -> assertEquals("bigint", column.get("data_type"), "version_number must be BIGINT"),
            () -> assertEquals("YES", column.get("is_nullable"), "version_number must allow NULL")
        );
    }
}
