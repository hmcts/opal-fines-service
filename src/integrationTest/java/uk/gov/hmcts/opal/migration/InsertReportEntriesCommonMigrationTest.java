package uk.gov.hmcts.opal.migration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@JiraEpic("PO-9048")
@JiraStory("PO-10348")
@DisplayName("Insert report entries common procedure integration tests")
class InsertReportEntriesCommonMigrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 77;
    private static final String REPORT_ID = "fp_register";
    private static final long REPORT_INSTANCE_ID = 99000000008000L;
    private static final String TEST_RECORD_PREFIX = "PO-10348-IT-";
    private static final String CALL_PROCEDURE =
        "CALL public.p_insert_report_entries_common(?, ?, ?, ?, ?, ?, ?, NULL)";

    @Autowired
    private DataSource dataSource;

    @AfterEach
    void deleteTestReportEntries() {
        jdbcTemplate.update(
            "DELETE FROM public.report_entries WHERE associated_record_id LIKE ?",
            TEST_RECORD_PREFIX + "%"
        );
    }

    @Test
    @DisplayName("PO-10348: inserts every supplied value and returns the generated identifier")
    void shouldInsertAllValuesAndReturnGeneratedId() throws SQLException {
        LocalDateTime entryTimestamp = LocalDateTime.of(2026, 11, 16, 10, 15, 30);
        LocalDateTime reportedTimestamp = LocalDateTime.of(2026, 11, 16, 10, 20, 45);
        String associatedRecordId = TEST_RECORD_PREFIX + "ALL";

        long reportEntryId = callProcedure(
            BUSINESS_UNIT_ID,
            REPORT_ID,
            entryTimestamp,
            reportedTimestamp,
            "report_instances",
            associatedRecordId,
            REPORT_INSTANCE_ID
        );

        Map<String, Object> insertedRow = findReportEntry(reportEntryId);
        assertAll(
            () -> assertEquals(Integer.valueOf(BUSINESS_UNIT_ID), insertedRow.get("business_unit_id")),
            () -> assertEquals(REPORT_ID, insertedRow.get("report_id")),
            () -> assertEquals(Timestamp.valueOf(entryTimestamp), insertedRow.get("entry_timestamp")),
            () -> assertEquals(Timestamp.valueOf(reportedTimestamp), insertedRow.get("reported_timestamp")),
            () -> assertEquals("report_instances", insertedRow.get("associated_record_type")),
            () -> assertEquals(associatedRecordId, insertedRow.get("associated_record_id")),
            () -> assertEquals(REPORT_INSTANCE_ID, insertedRow.get("report_instance_id"))
        );
    }

    @Test
    @DisplayName("PO-10348: preserves nullable values and generates a unique identifier for each call")
    void shouldPreserveNullsAndGenerateUniqueIds() throws SQLException {
        LocalDateTime entryTimestamp = LocalDateTime.of(2026, 11, 16, 11, 30);

        long firstReportEntryId = callProcedure(
            BUSINESS_UNIT_ID,
            REPORT_ID,
            entryTimestamp,
            null,
            "suspense_items",
            TEST_RECORD_PREFIX + "NULL-1",
            null
        );
        long secondReportEntryId = callProcedure(
            BUSINESS_UNIT_ID,
            REPORT_ID,
            entryTimestamp,
            null,
            "suspense_items",
            TEST_RECORD_PREFIX + "NULL-2",
            null
        );

        Map<String, Object> insertedRow = findReportEntry(firstReportEntryId);
        assertAll(
            () -> assertNotEquals(firstReportEntryId, secondReportEntryId),
            () -> assertNull(insertedRow.get("reported_timestamp")),
            () -> assertNull(insertedRow.get("report_instance_id"))
        );
    }

    @ParameterizedTest(name = "required parameter {0}")
    @ValueSource(strings = {
        "business_unit_id",
        "report_id",
        "entry_timestamp",
        "associated_record_type",
        "associated_record_id"
    })
    @DisplayName("PO-10348: propagates every not-null constraint violation to the caller")
    void shouldPropagateNotNullViolations(String nullParameter) {
        Short businessUnitId = BUSINESS_UNIT_ID;
        String reportId = REPORT_ID;
        LocalDateTime entryTimestamp = LocalDateTime.of(2026, 11, 16, 12, 0);
        String associatedRecordType = "suspense_items";
        String associatedRecordId = TEST_RECORD_PREFIX + "NULL-REQUIRED";

        switch (nullParameter) {
            case "business_unit_id" -> businessUnitId = null;
            case "report_id" -> reportId = null;
            case "entry_timestamp" -> entryTimestamp = null;
            case "associated_record_type" -> associatedRecordType = null;
            case "associated_record_id" -> associatedRecordId = null;
            default -> throw new IllegalArgumentException("Unexpected parameter: " + nullParameter);
        }

        Short finalBusinessUnitId = businessUnitId;
        String finalReportId = reportId;
        LocalDateTime finalEntryTimestamp = entryTimestamp;
        String finalAssociatedRecordType = associatedRecordType;
        String finalAssociatedRecordId = associatedRecordId;
        SQLException exception = assertThrows(SQLException.class, () -> callProcedure(
            finalBusinessUnitId,
            finalReportId,
            finalEntryTimestamp,
            null,
            finalAssociatedRecordType,
            finalAssociatedRecordId,
            null
        ));

        assertEquals("23502", exception.getSQLState());
    }

    @Test
    @DisplayName("PO-10348: propagates a business-unit foreign-key violation to the caller")
    void shouldPropagateBusinessUnitForeignKeyViolation() {
        SQLException exception = assertThrows(SQLException.class, () -> callProcedure(
            Short.MIN_VALUE,
            REPORT_ID,
            LocalDateTime.of(2026, 11, 16, 12, 15),
            null,
            "suspense_items",
            TEST_RECORD_PREFIX + "BAD-BU-FK",
            null
        ));

        assertEquals("23503", exception.getSQLState());
    }

    @Test
    @DisplayName("PO-10348: propagates foreign-key constraint violations to the caller")
    void shouldPropagateForeignKeyViolation() {
        SQLException exception = assertThrows(SQLException.class, () -> callProcedure(
            BUSINESS_UNIT_ID,
            "missing_report",
            LocalDateTime.of(2026, 11, 16, 12, 30),
            null,
            "suspense_items",
            TEST_RECORD_PREFIX + "BAD-FK",
            null
        ));

        assertEquals("23503", exception.getSQLState());
    }

    @Test
    @DisplayName("PO-10348: propagates a report-instance foreign-key violation to the caller")
    void shouldPropagateReportInstanceForeignKeyViolation() {
        SQLException exception = assertThrows(SQLException.class, () -> callProcedure(
            BUSINESS_UNIT_ID,
            REPORT_ID,
            LocalDateTime.of(2026, 11, 16, 12, 45),
            null,
            "suspense_items",
            TEST_RECORD_PREFIX + "BAD-INSTANCE-FK",
            Long.MAX_VALUE
        ));

        assertEquals("23503", exception.getSQLState());
    }

    @Test
    @DisplayName("PO-10348: accepts an associated record identifier at the column length boundary")
    void shouldAcceptMaximumAssociatedRecordIdLength() throws SQLException {
        String associatedRecordId = TEST_RECORD_PREFIX + "123456789012345678";

        long reportEntryId = callProcedure(
            BUSINESS_UNIT_ID,
            REPORT_ID,
            LocalDateTime.of(2026, 11, 16, 12, 50),
            null,
            "suspense_items",
            associatedRecordId,
            null
        );

        assertEquals(30, associatedRecordId.length());
        assertEquals(associatedRecordId, findReportEntry(reportEntryId).get("associated_record_id"));
    }

    @Test
    @DisplayName("PO-10348: rejects values outside the associated-record-type enum")
    void shouldRejectInvalidAssociatedRecordType() {
        SQLException exception = assertThrows(SQLException.class, () -> callProcedure(
            BUSINESS_UNIT_ID,
            REPORT_ID,
            LocalDateTime.of(2026, 11, 16, 12, 55),
            null,
            "invalid_record_type",
            TEST_RECORD_PREFIX + "BAD-ENUM",
            null
        ));

        assertEquals("22P02", exception.getSQLState());
    }

    @Test
    @DisplayName("PO-10348: propagates unexpected database exceptions to the caller")
    void shouldPropagateUnexpectedDatabaseException() {
        SQLException exception = assertThrows(SQLException.class, () -> callProcedure(
            BUSINESS_UNIT_ID,
            REPORT_ID,
            LocalDateTime.of(2026, 11, 16, 13, 0),
            null,
            "suspense_items",
            TEST_RECORD_PREFIX + "ASSOCIATED-RECORD-ID-EXCEEDS-THIRTY-CHARACTERS",
            null
        ));

        assertEquals("22001", exception.getSQLState());
    }

    private long callProcedure(Short businessUnitId, String reportId, LocalDateTime entryTimestamp,
                               LocalDateTime reportedTimestamp, String associatedRecordType,
                               String associatedRecordId, Long reportInstanceId) throws SQLException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CALL_PROCEDURE)) {
            setNullableShort(statement, 1, businessUnitId);
            statement.setString(2, reportId);
            setNullableTimestamp(statement, 3, entryTimestamp);
            setNullableTimestamp(statement, 4, reportedTimestamp);
            if (associatedRecordType == null) {
                statement.setNull(5, Types.OTHER);
            } else {
                statement.setObject(5, associatedRecordType, Types.OTHER);
            }
            statement.setString(6, associatedRecordId);
            setNullableLong(statement, 7, reportInstanceId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong("po_report_entry_id");
            }
        }
    }

    private Map<String, Object> findReportEntry(long reportEntryId) {
        return jdbcTemplate.queryForMap("""
            SELECT business_unit_id, report_id, entry_timestamp, reported_timestamp,
                   associated_record_type, associated_record_id, report_instance_id
            FROM public.report_entries
            WHERE report_entry_id = ?
            """, reportEntryId);
    }

    private void setNullableTimestamp(PreparedStatement statement, int parameterIndex, LocalDateTime value)
        throws SQLException {

        if (value == null) {
            statement.setNull(parameterIndex, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(parameterIndex, Timestamp.valueOf(value));
        }
    }

    private void setNullableShort(PreparedStatement statement, int parameterIndex, Short value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.SMALLINT);
        } else {
            statement.setShort(parameterIndex, value);
        }
    }

    private void setNullableLong(PreparedStatement statement, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.BIGINT);
        } else {
            statement.setLong(parameterIndex, value);
        }
    }
}
