package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Creates and removes minor-creditor history fixtures through the testing-support API.
 */
@Service
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class MinorCreditorHistoryFixtureService {

    private static final short BUSINESS_UNIT_ID = 77;
    private static final String CREDITOR_RECORD_TYPE = "creditor_accounts";
    private static final String DEFENDANT_RECORD_TYPE = "defendant_accounts";

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public MinorCreditorHistoryFixtureService(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    /**
     * Creates a self-contained minor-creditor history fixture.
     *
     * @param reference scenario reference used in visible test data fields.
     * @return identifiers and date window for the created fixture.
     */
    @Transactional
    public MinorCreditorHistoryFixture createFixture(String reference) {
        final String fixtureReference = reference == null || reference.isBlank() ? "MCHIST" : reference;
        final long partyId = nextVal("party_id_seq");
        final long creditorAccountId = nextVal("creditor_account_id_seq");
        final long defendantAccountId = nextVal("defendant_account_id_seq");
        final String creditorAccountNumber = "MCH" + creditorAccountId;
        final String defendantAccountNumber = "MCD" + defendantAccountId;

        final LocalDate excludedDate = LocalDate.now(clock).minusDays(2);
        final LocalDate dateFrom = excludedDate.plusDays(1);
        final LocalDate dateTo = excludedDate.plusDays(2);

        insertParty(partyId, fixtureReference);
        insertCreditorAccount(creditorAccountId, partyId, creditorAccountNumber);
        insertDefendantAccount(defendantAccountId, defendantAccountNumber);
        insertImposition(defendantAccountId, creditorAccountId, excludedDate);
        insertHistory(creditorAccountId, defendantAccountId, excludedDate, dateFrom, dateTo);

        return MinorCreditorHistoryFixture.builder()
            .creditorAccountId(creditorAccountId)
            .defendantAccountId(defendantAccountId)
            .partyId(partyId)
            .dateFrom(dateFrom)
            .dateTo(dateTo)
            .excludedDate(excludedDate)
            .build();
    }

    /**
     * Deletes a minor-creditor history fixture by creditor account id.
     *
     * @param creditorAccountId creditor account id returned by {@link #createFixture(String)}.
     */
    @Transactional
    public void deleteFixture(long creditorAccountId) {
        final Long partyId = queryLong(
            "SELECT minor_creditor_party_id FROM creditor_accounts WHERE creditor_account_id = ?",
            creditorAccountId
        );
        final Long defendantAccountId = queryLong(
            "SELECT defendant_account_id FROM impositions WHERE creditor_account_id = ? LIMIT 1",
            creditorAccountId
        );

        jdbcTemplate.update(
            "DELETE FROM notes WHERE associated_record_type = ?::public.t_associated_record_type_enum "
                + "AND associated_record_id = ?",
            CREDITOR_RECORD_TYPE,
            String.valueOf(creditorAccountId)
        );
        jdbcTemplate.update(
            "DELETE FROM amendments WHERE associated_record_type = ?::public.t_associated_record_type_enum "
                + "AND associated_record_id = ?",
            CREDITOR_RECORD_TYPE,
            String.valueOf(creditorAccountId)
        );
        jdbcTemplate.update("DELETE FROM creditor_transactions WHERE creditor_account_id = ?", creditorAccountId);
        jdbcTemplate.update("DELETE FROM impositions WHERE creditor_account_id = ?", creditorAccountId);
        jdbcTemplate.update("DELETE FROM creditor_accounts WHERE creditor_account_id = ?", creditorAccountId);

        if (partyId != null) {
            jdbcTemplate.update("DELETE FROM parties WHERE party_id = ?", partyId);
        }
        if (defendantAccountId != null) {
            jdbcTemplate.update("DELETE FROM defendant_accounts WHERE defendant_account_id = ?", defendantAccountId);
        }
    }

    private long nextVal(String sequenceName) {
        Long value = jdbcTemplate.queryForObject("SELECT nextval('public." + sequenceName + "')", Long.class);
        if (value == null) {
            throw new IllegalStateException("No value returned for sequence " + sequenceName);
        }
        return value;
    }

    private Long queryLong(String sql, long id) {
        return jdbcTemplate.query(sql, resultSet -> resultSet.next() ? resultSet.getLong(1) : null, id);
    }

    private void insertParty(long partyId, String reference) {
        jdbcTemplate.update(
            """
                INSERT INTO parties (
                  party_id, organisation, organisation_name, surname, forenames, title,
                  address_line_1, address_line_2, address_line_3, postcode, account_type,
                  last_changed_date
                )
                VALUES (?, false, null, ?, 'History', 'Mx',
                        '1 Fixture Street', 'History Quarter', 'Test City', 'MC1 1AA',
                        'Creditor', ?)
                """,
            partyId,
            reference,
            LocalDateTime.now(clock)
        );
    }

    private void insertCreditorAccount(long creditorAccountId, long partyId, String accountNumber) {
        jdbcTemplate.update(
            """
                INSERT INTO creditor_accounts (
                  creditor_account_id, business_unit_id, account_number, creditor_account_type,
                  prosecution_service, major_creditor_id, minor_creditor_party_id,
                  from_suspense, hold_payout, pay_by_bacs,
                  bank_sort_code, bank_account_number, bank_account_name, bank_account_reference,
                  bank_account_type, version_number, last_changed_date
                )
                VALUES (?, ?, ?, 'MN', true, null, ?, false, false, true,
                        '112233', '12345678', 'History Fixture', 'MCHIST',
                        '1', 4, ?)
                """,
            creditorAccountId,
            BUSINESS_UNIT_ID,
            accountNumber,
            partyId,
            LocalDateTime.now(clock)
        );
    }

    private void insertDefendantAccount(long defendantAccountId, String accountNumber) {
        jdbcTemplate.update(
            """
                INSERT INTO defendant_accounts (
                  defendant_account_id, business_unit_id, account_number,
                  amount_imposed, amount_paid, account_balance, account_status, account_type,
                  version_number
                )
                VALUES (?, ?, ?, 100.00, 60.00, 40.00, 'CS', 'Fine', 1)
                """,
            defendantAccountId,
            BUSINESS_UNIT_ID,
            accountNumber
        );
    }

    private void insertImposition(long defendantAccountId, long creditorAccountId, LocalDate postedDate) {
        jdbcTemplate.update(
            """
                INSERT INTO impositions (
                  imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name,
                  result_id, imposed_date, imposed_amount, paid_amount,
                  offence_title, offence_code, creditor_account_id
                )
                VALUES (?, ?, ?, 'MCHIMP', 'Minor Creditor History', 'FCOMP', ?, 100.00, 60.00,
                        'Minor creditor history fixture', 'MCHIST', ?)
                """,
            nextVal("imposition_id_seq"),
            defendantAccountId,
            at(postedDate, 7),
            at(postedDate, 7),
            creditorAccountId
        );
    }

    private void insertHistory(
        long creditorAccountId,
        long defendantAccountId,
        LocalDate excludedDate,
        LocalDate dateFrom,
        LocalDate dateTo) {

        insertAmendment(creditorAccountId, dateTo, 41, "hold-old", "hold-new");
        insertTransaction(creditorAccountId, defendantAccountId, dateTo, "MCHNEW", 30);
        insertNote(creditorAccountId, dateTo, "Newest minor creditor fixture note");

        insertTransaction(creditorAccountId, defendantAccountId, excludedDate, "MCHOLD", 10);
        insertNote(creditorAccountId, excludedDate, "Excluded minor creditor fixture note");
        insertAmendment(creditorAccountId, excludedDate, 43, "sort-old", "sort-new");

        insertNote(creditorAccountId, dateFrom, "Inside minor creditor fixture note");
        insertAmendment(creditorAccountId, dateFrom, 42, "bacs-old", "bacs-new");
        insertTransaction(creditorAccountId, defendantAccountId, dateFrom, "MCHIN", 20);
    }

    private void insertAmendment(
        long creditorAccountId,
        LocalDate postedDate,
        int fieldCode,
        String oldValue,
        String newValue) {

        jdbcTemplate.update(
            """
                INSERT INTO amendments (
                  amendment_id, business_unit_id, associated_record_type, associated_record_id,
                  amended_date, amended_by, amended_by_name, field_code, old_value, new_value,
                  case_reference, function_code
                )
                VALUES (?, ?, ?::public.t_associated_record_type_enum, ?, ?, 'MCHAMD', 'Minor Creditor History',
                        ?, ?, ?, null, 'minor-creditor-history')
                """,
            nextVal("amendment_id_seq"),
            BUSINESS_UNIT_ID,
            CREDITOR_RECORD_TYPE,
            String.valueOf(creditorAccountId),
            at(postedDate, 8),
            fieldCode,
            oldValue,
            newValue
        );
    }

    private void insertNote(long creditorAccountId, LocalDate postedDate, String noteText) {
        jdbcTemplate.update(
            """
                INSERT INTO notes (
                  note_id, note_type, associated_record_type, associated_record_id,
                  note_text, posted_date, posted_by, posted_by_name
                )
                VALUES (?, 'AA', ?::public.t_associated_record_type_enum, ?, ?, ?, 'MCHNOTE',
                        'Minor Creditor History')
                """,
            nextVal("note_id_seq"),
            CREDITOR_RECORD_TYPE,
            String.valueOf(creditorAccountId),
            noteText,
            at(postedDate, 9)
        );
    }

    private void insertTransaction(
        long creditorAccountId,
        long defendantAccountId,
        LocalDate postedDate,
        String reference,
        int amount) {

        jdbcTemplate.update(
            """
                INSERT INTO creditor_transactions (
                  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
                  transaction_type, transaction_amount, imposition_result_id, payment_processed,
                  payment_reference, status, status_date, associated_record_type, associated_record_id
                )
                VALUES (?, ?, ?, 'MCHFIN', 'Minor Creditor History', 'PAYMNT', ?, null, true,
                        ?, 'C', ?, ?::public.t_associated_record_type_enum, ?)
                """,
            nextVal("creditor_transaction_id_seq"),
            creditorAccountId,
            at(postedDate, 10),
            amount,
            reference,
            at(postedDate, 11),
            DEFENDANT_RECORD_TYPE,
            String.valueOf(defendantAccountId)
        );
    }

    private LocalDateTime at(LocalDate date, int hour) {
        return LocalDateTime.of(date, LocalTime.of(hour, 0));
    }

    /**
     * Identifiers and filter window for a created minor-creditor history fixture.
     */
    @Builder
    public record MinorCreditorHistoryFixture(
        @JsonProperty("creditor_account_id")
        Long creditorAccountId,
        @JsonProperty("defendant_account_id")
        Long defendantAccountId,
        @JsonProperty("party_id")
        Long partyId,
        @JsonProperty("date_from")
        LocalDate dateFrom,
        @JsonProperty("date_to")
        LocalDate dateTo,
        @JsonProperty("excluded_date")
        LocalDate excludedDate
    ) {
    }
}
