/**
* CGI OPAL Program
*
* MODULE      : impositions_creditor_names_test_data.sql
*
* DESCRIPTION : Dev-only synthetic test data for PO-10783 / PO-10571 defendant account imposition creditor-name scenarios.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ------------------------------------------------------------------------------------
* 30/09/2026    P Brumby       1.0         PO-10783 Test data for PO-10571 impositions creditor-name display scenarios
* 08/10/2026    P Brumby       1.1         PO-10783 Test data Add payment_terms record
*
**/

-- ---------------------------------------------------------------------------
-- Dev-only seeded data for PO-10571 / PO-10783.
--
-- Business unit: 65, Camden and Islington.
-- Posted by    : L065JG / opal-test.
--
-- Scenarios:
-- 1. Minor creditor organisation name.
-- 2. Minor creditor individual with forenames and surname.
-- 3. Minor creditor individual with surname only.
-- 4. Major creditor name and major-creditor link.
-- 5. Central Fund, displayed without a link.
-- 6. Minor creditor organisation with missing company name, displayed as Minor Creditor.
--
-- Assumptions:
-- - The seeded Camden and Islington business unit (65) has a single Central Fund creditor account
--   with creditor_account_type = 'CF'.
-- 
-- ---------------------------------------------------------------------------

-- Clear any existing PO-10783 / PO-10571 synthetic data for repeatable Dev builds.

DELETE FROM public.payment_terms
 WHERE payment_terms_id = 99105710000601;

DELETE FROM public.impositions
 WHERE imposition_id BETWEEN 99105710000301 AND 99105710000306;

DELETE FROM public.creditor_accounts
 WHERE creditor_account_id BETWEEN 99105710000201 AND 99105710000205;

DELETE FROM public.major_creditors
 WHERE major_creditor_id = 99105710000401;

DELETE FROM public.defendant_account_parties
 WHERE defendant_account_party_id = 99105710000501;

DELETE FROM public.parties
 WHERE party_id BETWEEN 99105710000101 AND 99105710000105;

DELETE FROM public.defendant_accounts
 WHERE defendant_account_id = 99105710000001;

-- ---------------------------------------------------------------------------
-- Defendant account parent record.
-- ---------------------------------------------------------------------------

INSERT INTO public.defendant_accounts (
    defendant_account_id, business_unit_id, account_number, imposed_hearing_date,
    imposing_court_id, amount_imposed, amount_paid, account_balance, account_status,
    completed_date, enforcing_court_id, last_hearing_court_id, last_hearing_date,
    last_movement_date, last_changed_date, last_enforcement, originator_name,
    originator_type, allow_writeoffs, allow_cheques, cheque_clearance_period,
    credit_trans_clearance_period, enf_override_result_id, enf_override_enforcer_id,
    enf_override_tfo_lja_id, unit_fine_detail, unit_fine_value, collection_order,
    collection_order_date, further_steps_notice_date, confiscation_order_date,
    fine_registration_date, suspended_committal_date, consolidated_account_type,
    payment_card_requested, payment_card_requested_date, payment_card_requested_by,
    prosecutor_case_reference, enforcement_case_status, originator_id, account_type,
    account_comments, account_note_1, account_note_2, account_note_3, jail_days,
    version_number, payment_card_requested_by_name, imposed_by_name
) VALUES (
    99105710000001, 65, '10571001A', '2026-09-21 09:00:00',
    50000000001, -621.00, 0.00, -621.00, 'L',
    NULL, 50000000001, 50000000001, '2026-09-21 09:00:00',
    '2026-09-22 09:06:00', '2026-09-22 09:06:00', NULL, 'PO-10783 Camden and Islington',
    'NEW', true, true, 5,
    3, NULL, NULL,
    NULL, NULL, NULL, false,
    NULL, NULL, NULL,
    '2026-09-21 09:00:00', NULL, NULL,
    false, NULL, NULL,
    'PO10783-PO10571', NULL, 'PO10571CI01', 'Fine',
    'PO-10783 test account for PO-10571 impositions',
    'Synthetic imposition creditor-name data',
    'Camden and Islington defendant',
    'Dev only',
    NULL, 1, NULL, 'New'
);

-- ---------------------------------------------------------------------------
-- Parties: one defendant and four minor-creditor parties.
-- ---------------------------------------------------------------------------

INSERT INTO public.parties (
    party_id, organisation, organisation_name, surname, forenames, title,
    address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
    postcode, account_type, birth_date, age, national_insurance_number,
    telephone_home, telephone_business, telephone_mobile, email_1, email_2,
    last_changed_date
) VALUES
    (99105710000101, false, NULL, 'Imposition', 'Polly Olivia', 'Ms',
     '1 PO-10571 Test Street', 'Camden', NULL, NULL, NULL,
     'N1 1AA', 'Defendant', '1985-01-01 00:00:00', NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    (99105710000102, true, 'PO10571 Company Ltd', NULL, NULL, NULL,
     '2 PO-10571 Creditor Street', 'Camden', NULL, NULL, NULL,
     'N1 2AA', 'Creditor', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    (99105710000103, false, NULL, 'Example', 'Alex James', NULL,
     '3 PO-10571 Creditor Street', 'Camden', NULL, NULL, NULL,
     'N1 3AA', 'Creditor', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    (99105710000104, false, NULL, 'SurnameOnly', NULL, NULL,
     '4 PO-10571 Creditor Street', 'Camden', NULL, NULL, NULL,
     'N1 4AA', 'Creditor', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    -- Deliberately missing organisation_name to exercise UI fallback to "Minor Creditor".
    (99105710000105, true, NULL, NULL, NULL, NULL,
     '5 PO-10571 Creditor Street', 'Camden', NULL, NULL, NULL,
     'N1 5AA', 'Creditor', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO public.defendant_account_parties (
    defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
) VALUES (
    99105710000501, 99105710000001, 99105710000101, 'Defendant', true
);

-- ---------------------------------------------------------------------------
-- Creditor records.
-- ---------------------------------------------------------------------------

INSERT INTO public.major_creditors (
    major_creditor_id, business_unit_id, major_creditor_code, name,
    address_line_1, address_line_2, address_line_3, postcode,
    contact_name, contact_telephone, contact_email
) VALUES (
    99105710000401, 65, 'P571', 'PO10571 Major Creditor',
    '6 PO-10571 Major Creditor Street', 'Camden', NULL, 'N1 6AA',
    'PO10571 Contact', NULL, NULL);

INSERT INTO public.creditor_accounts (
    creditor_account_id, business_unit_id, account_number, creditor_account_type,
    prosecution_service, major_creditor_id, minor_creditor_party_id,
    repayment, hold_payout, pay_by_bacs,
    bank_sort_code, bank_account_number, bank_account_name,
    bank_account_reference, bank_account_type, last_changed_date, version_number
) VALUES
    (99105710000201, 65, 'PO10571MC01', 'MN',
     false, NULL, 99105710000102,
     false, false, true,
     '112233', '87654321', 'PO10571 Company',
     'PO10571MC01', '1', NULL, 1),

    (99105710000202, 65, 'PO10571MC02', 'MN',
     false, NULL, 99105710000103,
     false, false, true,
     '112233', '87654322', 'Alex Example',
     'PO10571MC02', '1', NULL, 1),

    (99105710000203, 65, 'PO10571MC03', 'MN',
     false, NULL, 99105710000104,
     false, false, true,
     '112233', '87654323', 'SurnameOnly',
     'PO10571MC03', '1', NULL, 1),

    (99105710000204, 65, 'PO10571MC04', 'MN',
     false, NULL, 99105710000105,
     false, false, true,
     '112233', '87654324', 'Missing Name',
     'PO10571MC04', '1', NULL, 1),

    (99105710000205, 65, 'PO10571MJ01', 'MJ',
     false, 99105710000401, NULL,
     false, false, true,
     '112233', '87654325', 'PO10571 Major',
     'PO10571MJ01', '1', NULL, 1);

-- ---------------------------------------------------------------------------
-- Impositions.
-- ---------------------------------------------------------------------------

INSERT INTO public.impositions (
    imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name,
    original_posted_date, result_id, imposing_court_id, imposed_date,
    imposed_amount, paid_amount, offence_id, offence_title, offence_code,
    creditor_account_id, unit_fine_adjusted, unit_fine_units, completed,
    original_imposition_id
) VALUES
    -- Minor creditor company.
    (99105710000301, 99105710000001, '2026-09-22 09:01:00', 'L065JG', 'opal-test',
     NULL, 'FO', 50000000001, '2026-09-21 00:00:00',
     -101.00, 0.00, NULL, 'PO10571 company creditor', 'P57101',
     99105710000201, NULL, NULL, false, NULL),

    -- Minor creditor individual with full name.
    (99105710000302, 99105710000001, '2026-09-22 09:02:00', 'L065JG', 'opal-test',
     NULL, 'FO', 50000000001, '2026-09-21 00:00:00',
     -102.00, 0.00, NULL, 'PO10571 individual full name', 'P57102',
     99105710000202, NULL, NULL, false, NULL),

    -- Minor creditor individual with surname only.
    (99105710000303, 99105710000001, '2026-09-22 09:03:00', 'L065JG', 'opal-test',
     NULL, 'FO', 50000000001, '2026-09-21 00:00:00',
     -103.00, 0.00, NULL, 'PO10571 individual surname only', 'P57103',
     99105710000203, NULL, NULL, false, NULL),

    -- Major creditor.
    (99105710000304, 99105710000001, '2026-09-22 09:04:00', 'L065JG', 'opal-test',
     NULL, 'FO', 50000000001, '2026-09-21 00:00:00',
     -104.00, 0.00, NULL, 'PO10571 major creditor', 'P57104',
     99105710000205, NULL, NULL, false, NULL),

    -- Central Fund. Expects seeded BU 65 Central Fund creditor account to exist.
    (99105710000305, 99105710000001, '2026-09-22 09:05:00', 'L065JG', 'opal-test',
     NULL, 'FO', 50000000001, '2026-09-21 00:00:00',
     -105.00, 0.00, NULL, 'PO10571 Central Fund', 'P57105',
     (
         SELECT creditor_account_id
           FROM public.creditor_accounts
          WHERE business_unit_id = 65
            AND creditor_account_type = 'CF'
     ), NULL, NULL, false, NULL),

    -- Missing company name fallback.
    (99105710000306, 99105710000001, '2026-09-22 09:06:00', 'L065JG', 'opal-test',
     NULL, 'FO', 50000000001, '2026-09-21 00:00:00',
     -106.00, 0.00, NULL, 'PO10571 missing company name fallback', 'P57106',
     99105710000204, NULL, NULL, false, NULL);

-- ---------------------------------------------------------------------------
--  payment terms
-- ---------------------------------------------------------------------------

-- By date 2026-10-22, the defendant account will have a balance of -621.00, which is the sum of the impositions above.
INSERT INTO public.payment_terms (payment_terms_id,defendant_account_id,posted_date,posted_by,terms_type_code,effective_date,extension,account_balance,posted_by_name,active) VALUES 
     (99105710000601,99105710000001,'2026-09-22 09:06:00.000','L065JG', 'B', '2026-10-22', false, -621.00, 'opal-test', true);
