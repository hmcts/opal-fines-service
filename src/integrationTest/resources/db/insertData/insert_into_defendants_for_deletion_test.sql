-- File: insert_into_defendants_for_deletion_test.sql
-- Enhanced test data for comprehensive deletion testing

-- Add a creditor account for the test (this is what impositions references, not major_creditors)
INSERT INTO creditor_accounts (creditor_account_id, business_unit_id, account_number,
                               creditor_account_type,
                               prosecution_service, from_suspense, hold_payout, pay_by_bacs)
VALUES (9200, 78, 'CRED001', 'MN',
        false, false, false, false);

-- Add required court reference
INSERT INTO courts (court_id, business_unit_id, court_code, name)
VALUES (9201, 78, 123, 'Test Court');

-- Add required offence reference
INSERT INTO offences (offence_id, cjs_code, business_unit_id, offence_title)
VALUES (9202, 'TEST001', 78, 'Test Offence');

-- Add required result reference
INSERT INTO results (result_id, result_title, result_type, active, imposition,
                     imposition_accruing, enforcement, enforcement_override,
                     further_enforcement_warn, further_enforcement_disallow, enforcement_hold,
                     requires_enforcer, generates_hearing, generates_warrant, collection_order,
                     extend_ttp_disallow, extend_ttp_preserve_last_enf, prevent_payment_card,
                     lists_monies)
VALUES ('TSTRES', 'Test Result', 'Result', true, true,
        false, false, false,
        false, false, false,
        false, false, false, false,
        false, false, false, false);

-- Insert the main defendant account
INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                imposed_hearing_date, imposing_court_id, amount_imposed,
                                amount_paid, account_balance, account_status, completed_date,
                                enforcing_court_id, last_hearing_court_id, last_hearing_date,
                                last_movement_date, last_changed_date, last_enforcement,
                                originator_name, originator_id, originator_type,
                                allow_writeoffs, allow_cheques, cheque_clearance_period,
                                credit_trans_clearance_period,
                                enf_override_result_id, enf_override_enforcer_id,
                                enf_override_tfo_lja_id,
                                unit_fine_detail, unit_fine_value, collection_order,
                                collection_order_date,
                                further_steps_notice_date, confiscation_order_date,
                                fine_registration_date, suspended_committal_date,
                                consolidated_account_type, payment_card_requested,
                                payment_card_requested_date, payment_card_requested_by,
                                prosecutor_case_reference, enforcement_case_status, account_type,
                                version_number)
VALUES (1001, 78, '100B',
        '2023-11-03 16:05:10', 9201, 700.58,
        200.00, 500.58, 'L', NULL,
        9201, 9201, '2024-01-04 18:06:11',
        '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'REM',
        'Kingston-upon-Thames Mags Court', NULL, NULL,
        'N', 'N', 14, 21,
        NULL, NULL, NULL,
        'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00',
        '2023-12-19 00:00:00', NULL, NULL, NULL,
        'Y', 'Y', '2024-01-01 00:00:00', '11111111A',
        '090A', NULL, 'Fine', 1);

--  Ensure the version is initialized so deletes include a bound value
UPDATE defendant_accounts
SET version_number = 0
WHERE defendant_account_id = 1001
  AND version_number IS NULL;

-- Insert party
INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title,
                     address_line_1, address_line_2, address_line_3,
                     address_line_4, address_line_5, postcode,
                     account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES (9101, 'N', NULL,
        'Graham', 'Anna', 'Ms',
        'Lumber House', '54 Gordon Road', 'Maidstone, Kent',
        NULL, NULL, 'MA4 1AL',
        'Debtor', '1980-02-03 00:00:00', 33, 'A11111A', NULL);

-- Insert defendant account parties (Level 2)
INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (9102, 1001, 9101,
        'Defendant', 'Y');

-- Insert payment terms (Level 2)
INSERT INTO payment_terms (payment_terms_id, defendant_account_id, posted_date, posted_by,
                           terms_type_code, effective_date, instalment_period, instalment_amount,
                           instalment_lump_sum,
                           jail_days, extension, account_balance)
VALUES (9103, 1001, '2023-11-03 16:05:10', '01000000A',
        'B', '2025-10-12 00:00:00', NULL, NULL, NULL,
        120, 'N', 700.58);

-- Insert defendant transaction (Level 2)
INSERT INTO defendant_transactions (defendant_transaction_id, defendant_account_id, posted_date,
                                    posted_by,
                                    transaction_type, transaction_amount, payment_method,
                                    payment_reference,
                                    text, status, status_date, status_amount, posted_by_name)
VALUES (9104, 1001, '2023-11-04', '01000000A',
        'PAY', 100.00, 'CH', 'CHQ123',
        'Cheque payment', 'A', '2023-11-04', 100.00, 'User9100');

-- Insert imposition (Level 2) - Now with correct foreign key references
INSERT INTO impositions (imposition_id, defendant_account_id, posted_date, posted_by,
                         posted_by_name,
                         result_id, imposing_court_id, imposed_date, imposed_amount, paid_amount,
                         offence_id, creditor_account_id, unit_fine_adjusted, completed)
VALUES (9105, 1001, '2023-11-03 16:05:10', '01000000A', 'User9100',
        'TSTRES', 9201, '2023-11-03 16:05:10', 700.00, 200.00,
        9202, 9200, false, false);

-- Insert allocations (Level 3 - references both imposition and defendant_transaction)
INSERT INTO allocations (allocation_id, imposition_id, defendant_transaction_id,
                         allocated_date, allocated_amount, transaction_type, allocation_function)
VALUES (9106, 9105, 9104, '2023-11-04 10:00:00', 100.00, 'Payment', 'Auto');

-- Insert another allocation (Level 3 - only references imposition)
INSERT INTO allocations (allocation_id, imposition_id, allocated_date, allocated_amount,
                         transaction_type, allocation_function)
VALUES (9108, 9105, '2023-11-05 10:00:00', 50.00, 'Adjustment', 'Manual');

-- Insert cheque (Level 3 - references defendant_transaction)
INSERT INTO cheques (cheque_id, business_unit_id, cheque_number, issue_date,
                     defendant_transaction_id, amount, status)
VALUES (9107, 78, 123456, '2023-11-04 10:00:00',
        9104, 100.00, 'C');

INSERT INTO notes(note_id, note_type, associated_record_type,
                         associated_record_id, note_text, posted_date,
                         posted_by, posted_by_name)
VALUES (1, 'TE', 'DEF', '1001',
        'testData', '2025-10-27 15:49:42.498414+00',
        '01000000A', 'User9100');

