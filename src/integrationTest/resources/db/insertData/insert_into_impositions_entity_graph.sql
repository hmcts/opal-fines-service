/**
 * OPAL Program
 *
 * MODULE      : insert_into_impositions_entity_graph.sql
 *
 * DESCRIPTION : Inserts Imposition entity graph data for integration tests
 *
 * VERSION HISTORY:
 *
 * Date          Author       Version     Nature of Change
 * ----------    --------     --------    ----------------------------------------------------------------
 * 17/04/2026    S WILLIAMS   1.0         PO-2884 Insert rows of data into IMPOSITIONS and related tables for integration tests
 *
 **/

INSERT INTO business_units (
    business_unit_id, business_unit_name, business_unit_code, business_unit_type, welsh_language
    )
VALUES
    (
     55, 'Graph Test Business Unit', 'GT', 'Area', false
    );

INSERT INTO local_justice_areas (
    local_justice_area_id, lja_code, name, address_line_1, address_line_4, address_line_5, end_date
    )
VALUES
    (
    5501, 'IGL1', 'Graph Test LJA', '3 LJA Street', NULL, NULL, NULL
    );

INSERT INTO courts (
    court_id, business_unit_id, court_code, parent_court_id, name, name_cy, address_line_1,
    address_line_2, address_line_3, address_line_1_cy, address_line_2_cy, address_line_3_cy,
    postcode, local_justice_area_id, national_court_code, gob_enforcing_court_code, lja,
    court_type, division, session, start_time, max_load, record_session_times, max_court_duration,
    group_code
    )
VALUES
    (
    551001, 55, 101, NULL, 'Graph Test Court',
    NULL, '1 Justice Street', NULL, NULL, NULL, NULL, NULL,
    'GT1 1GT', 5501, NULL, NULL,
    5501, 'MC', '01', NULL, NULL, NULL, NULL, NULL, NULL
    );

INSERT INTO results (
    result_id, result_title, result_title_cy, result_type, active, imposition, imposition_category,
    imposition_allocation_priority, imposition_accruing, imposition_creditor, enforcement,
    enforcement_override, further_enforcement_warn, further_enforcement_disallow, enforcement_hold,
    requires_enforcer, generates_hearing, generates_warrant, collection_order, extend_ttp_disallow,
    extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, result_parameters, manual_enforcement
    )
VALUES
    (
    'IGR001', 'Imposition Graph Result', NULL, 'Action', TRUE,
    TRUE, 'Compensation', 1, FALSE, 'CF',
    FALSE, FALSE, FALSE, FALSE, FALSE,
    FALSE, FALSE, FALSE, FALSE,
    FALSE, FALSE, FALSE,
    FALSE, NULL, FALSE
    );

INSERT INTO offences (
    offence_id, cjs_code, offence_title, offence_title_cy, offence_oas, offence_oas_cy,
    date_used_from, date_used_to
    )
VALUES
    (
    5510, 'IG5510', 'Imposition Graph Offence', NULL, 'Graph offence OAS',
    NULL, DATE '2025-01-01', NULL
    );

INSERT INTO major_creditors (
    major_creditor_id, business_unit_id, major_creditor_code, name, address_line_1, address_line_2,
    address_line_3, postcode
    )
VALUES
    (
    551003, 55, 'IGMC', 'Graph Major Creditor', '2 Creditor Road',
    NULL, NULL, 'GT1 2MC'
    );

INSERT INTO creditor_accounts (
    creditor_account_id, business_unit_id, account_number, creditor_account_type, prosecution_service,
    major_creditor_id, minor_creditor_party_id, from_suspense, hold_payout, pay_by_bacs,
    bank_sort_code, bank_account_number, bank_account_name, bank_account_reference, bank_account_type,
    version_number, last_changed_date
    )
VALUES
    (
    551004, 55, 'IG551004', 'MJ', TRUE, 551003,
    NULL, FALSE, FALSE, TRUE, '112233', '12345678',
    'Graph Creditor', 'IG-REF-01', '1', 1, TIMESTAMP '2026-04-17 09:00:00'
    );

INSERT INTO defendant_accounts (
    defendant_account_id, business_unit_id, account_number, imposing_court_id,
    amount_imposed, amount_paid, account_balance, account_status, account_type
    )
VALUES
    (
    551002, 55, 'IG551002', 551001, 250.00,
    25.00, 225.00, 'L', 'Fine'
    );

INSERT INTO impositions (
    imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name, original_posted_date,
    result_id, imposing_court_id, imposed_date, imposed_amount, paid_amount, offence_id,
    creditor_account_id, unit_fine_adjusted, unit_fine_units, completed
    )
VALUES
    (
    551005, 551002, TIMESTAMP '2026-04-17 10:00:00', '99999999A', 'Graph User',
    NULL, 'IGR001', 551001, TIMESTAMP '2026-04-16 09:30:00', 250.00,
    25.00, 5510, 551004, FALSE, 3, FALSE
    );
