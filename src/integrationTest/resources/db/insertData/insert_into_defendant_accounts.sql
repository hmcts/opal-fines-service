/**
* OPAL Program
*
* MODULE      : insert_into_defendants.sql
*
* DESCRIPTION : Inserts rows of data into the DEFENDANT_ACCOUNTS table for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author   Version  Nature of Change
* ----------  -------  -------  -----------------------------------------------------------------------------------------
* 02/06/2025  R DODD   1.0      PO-1047 Inserts rows of data into the DEFENDANT_ACCOUNTS table for the Integration Tests.
*
**/

-- Make sure we’re operating in the expected schema
SET search_path TO public;

-- Columns used below that might not exist in some environments
ALTER TABLE public.enforcers
  ADD COLUMN IF NOT EXISTS name varchar(255);

ALTER TABLE public.local_justice_areas
  ADD COLUMN IF NOT EXISTS name varchar(255);

-- Ensure BU 78 exists for joins
INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type,
                            welsh_language)
VALUES (78, 'N E Region', 'NE', 'Area', FALSE)
ON CONFLICT (business_unit_id) DO UPDATE
    SET business_unit_name = EXCLUDED.business_unit_name,
        business_unit_code = EXCLUDED.business_unit_code,
        business_unit_type = EXCLUDED.business_unit_type,
        welsh_language     = EXCLUDED.welsh_language;


-- --  Enforcement Override Result: FWEC
-- INSERT INTO enforcement_override_results (enforcement_override_result_id, enforcement_override_result_name)
-- VALUES ('FWEC', 'WITNESS EXPENSES - CENTRAL FUNDS')
-- ON CONFLICT (enforcement_override_result_id) DO UPDATE
--   SET enforcement_override_result_name = EXCLUDED.enforcement_override_result_name;

--  Enforcer referenced by enf_override_enforcer_id = 780000000021
INSERT INTO enforcers (enforcer_id, business_unit_id, enforcer_code, name, warrant_reference_sequence, warrant_register_sequence)
VALUES (780000000021, 78, 21, 'North East Enforcement', NULL, NULL)
ON CONFLICT (enforcer_id) DO UPDATE
  SET business_unit_id = EXCLUDED.business_unit_id,
      enforcer_code    = EXCLUDED.enforcer_code,
      name             = EXCLUDED.name;

--  Small-ID Enforcer for integration tests (fits in 32-bit int)
INSERT INTO enforcers (enforcer_id, business_unit_id, enforcer_code, name, warrant_reference_sequence, warrant_register_sequence)
VALUES (21, 78, 21, 'North East Enforcement', NULL, NULL)
ON CONFLICT (enforcer_id) DO NOTHING;

-- Local Justice Areas referenced by enf_override_tfo_lja_id
INSERT INTO local_justice_areas
  (local_justice_area_id, lja_code, name, address_line_1, address_line_4, address_line_5, end_date)
VALUES
  (240, 'L240', 'Tyne & Wear LJA', 'Test LJA Address Line 1', NULL, NULL, NULL),
  (241, 'L241', 'Wearside LJA', 'Test LJA Address Line 2', NULL, NULL, NULL)
ON CONFLICT (local_justice_area_id) DO UPDATE
  SET lja_code = EXCLUDED.lja_code,
      name     = EXCLUDED.name,
      address_line_1 = EXCLUDED.address_line_1;

--  Enforcing court used by enforcing_court_id = 780000000185
INSERT INTO courts (court_id, business_unit_id, court_code, name)
VALUES (780000000185, 78, 17, 'CH17')
ON CONFLICT (court_id) DO UPDATE
  SET business_unit_id = EXCLUDED.business_unit_id,
      court_code       = EXCLUDED.court_code,
      name             = EXCLUDED.name;

--  Small-ID court for integration tests (fits in 32-bit int)
INSERT INTO courts (court_id, business_unit_id, court_code, name)
VALUES (100, 78, 100, 'Central Magistrates')
ON CONFLICT (court_id) DO UPDATE
  SET business_unit_id = EXCLUDED.business_unit_id,
      court_code       = EXCLUDED.court_code,
      name             = EXCLUDED.name;

-- Basic setup for document template.
INSERT INTO documents (document_id, recipient, document_language, priority)
VALUES ('TTPLET', 'DEF', 'EN', 0)
ON CONFLICT (document_id) DO NOTHING;

INSERT INTO defendant_accounts
( defendant_account_id, version_number, business_unit_id, account_number
, imposed_hearing_date, imposing_court_id, amount_imposed
, amount_paid, account_balance, account_status, completed_date
, enforcing_court_id, last_hearing_court_id, last_hearing_date
, last_movement_date, last_changed_date, last_enforcement
, originator_name, originator_id, originator_type
, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
, unit_fine_detail, unit_fine_value, collection_order, collection_order_date
, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
, prosecutor_case_reference, enforcement_case_status, account_type
, account_comments, account_note_1, account_note_2, account_note_3
, jail_days
)
VALUES ( 0077, 0, 078, '177A'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-01-04 18:06:11'
       , '2024-01-02 17:08:09', '2024-01-03 12:00:12', '10'
       , 'Kingston-upon-Thames Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00'
       , '2023-12-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2024-01-01 00:00:00', '11111111A'
       , '090A', NULL, 'Fine'
       , 'Text - Account Comment', 'free_text_note_1', 'free_text_note_2', 'free_text_note_3'
       , 101
       ),
       ( 0078, 20, 078, '178A'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-02-24 18:06:11'
       , '2025-01-02 17:08:09', '2025-01-03 12:00:12', 'MPSO'
       , 'Kingston-upon-Thames Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2024-02-18 00:00:00'
       , '2024-02-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2025-01-01 00:00:00', '11111111A'
       , '099B', NULL, 'Fine'
       , 'Text', NULL, NULL, 'text_note_3'
       , 101
       );

INSERT INTO parties
( party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES ( 0077, 'N', 'Sainsco'
       , 'Graham', 'Anna', 'Ms'
       , 'Lumber House', '77 Gordon Road', 'Maidstone, Kent'
       , NULL, NULL, 'MA4 1AL'
       , 'Debtor', '1980-02-03 00:00:00', 33, 'A11111A', NULL),
       ( 0078, 'N', 'Sainsco'
       , 'Wilkins', 'Dave', 'Mr'
       , 'Lumber House', '78 Gordon Road', 'Maidstone, Kent'
       , NULL, NULL, 'MA5 1AL'
       , 'Debtor', '1970-02-03 00:00:00', 43, 'A11111A', NULL);

INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_address_line_2,
  employer_address_line_3, employer_address_line_4, employer_address_line_5,
  employer_postcode, employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
( 77, 'Toyota Prius', 'AB77CDE',
  'Tesco Ltd', '123 Employer Road', NULL,
  NULL, NULL, NULL,
  'EMP1 2AA', 'EMPREF77', '02079997777', 'employer77@company.com',
  'EN', NULL, 'EN', NULL ),
( 78, 'Toyota Prius', 'AB77CDE',
  'Tesco Ltd', '123 Employer Road', NULL,
  NULL, NULL, NULL,
  'EMP1 2AA', 'EMPREF78', '02079997777', 'employer78@company.com',
  'EN', NULL, 'EN', NULL );


INSERT INTO fixed_penalty_offences
(defendant_account_id, ticket_number)
VALUES (77, '888');
INSERT INTO defendant_account_parties
( defendant_account_party_id, defendant_account_id, party_id
, association_type, debtor)
VALUES ( 0077, 0077, 0077
       , 'Defendant', 'Y'),
       ( 0078, 0078, 0078
       , 'Defendant', 'Y');

-- Enhance fixed penalty offence for account 77 (for PO-1819 integration test completeness)
UPDATE fixed_penalty_offences
SET
    vehicle_registration = 'AB12CDE',
    offence_location = 'London',
    notice_number = 'PN98765',
    issued_date = '2024-01-01',
    licence_number = 'DOE1234567',
    vehicle_fixed_penalty = TRUE,
    offence_time = '12:34'
WHERE defendant_account_id = 77;

INSERT INTO payment_terms
( payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance)
VALUES ( 0077, 0077, '2023-11-03 16:05:10', '01000000A'
       , 'B', '2025-10-12 00:00:00', 'W', NULL, NULL
       , 120, 'N', 700.58);

UPDATE payment_terms
SET active = TRUE
WHERE payment_terms_id = 77
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');

-- PO-2629 isolated seed data
-- Account A: multiple payment_terms, exactly one active
INSERT INTO defendant_accounts
(defendant_account_id, version_number, business_unit_id, account_number,
 amount_paid, account_balance, amount_imposed, account_status,
 prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
 collection_order, payment_card_requested)
VALUES (262901, 0, 78, '262901A',
        0.00, 500.00, 500.00, 'L',
        '262901PCR', 'N', 'N', 'Fine',
        'N', 'N')
    ON CONFLICT (defendant_account_id) DO NOTHING;

INSERT INTO parties
(party_id, organisation, organisation_name, surname, forenames, title)
VALUES (262901, 'N', NULL, 'PO2629', 'MultiTerms', 'Mr')
    ON CONFLICT (party_id) DO NOTHING;

INSERT INTO defendant_account_parties
(defendant_account_party_id, defendant_account_id, party_id, association_type, debtor)
VALUES (262901, 262901, 262901, 'Defendant', 'Y')
    ON CONFLICT (defendant_account_party_id) DO NOTHING;

-- Active payment term
INSERT INTO payment_terms
(payment_terms_id, defendant_account_id, posted_date, posted_by,
 terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum,
 jail_days, extension, account_balance)
VALUES (26290101, 262901, '2023-11-03 16:05:10', '01000000A',
        'B', '2025-10-12 00:00:00', 'W', NULL, NULL,
        120, 'N', 500.00)
    ON CONFLICT (payment_terms_id) DO NOTHING;

UPDATE payment_terms
SET active = TRUE
WHERE payment_terms_id = 26290101
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');

-- Inactive payment term
INSERT INTO payment_terms
(payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance)
VALUES (26290102, 262901, '2024-01-10 10:00:00', '01000000A'
       , 'B', '2026-01-01 00:00:00', 'M', 123.45, NULL
       , 10, 'N', 500.00)
    ON CONFLICT (payment_terms_id) DO NOTHING;

UPDATE payment_terms
SET active = FALSE
WHERE payment_terms_id = 26290102
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');


-- Account B: payment_terms exist but none active
INSERT INTO defendant_accounts
(defendant_account_id, version_number, business_unit_id, account_number,
 amount_paid, account_balance, amount_imposed, account_status,
 prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
 collection_order, payment_card_requested)
VALUES (262902, 0, 78, '262902A',
        0.00, 500.00, 500.00, 'L',
        '262902PCR', 'N', 'N', 'Fine',
        'N', 'N')
    ON CONFLICT (defendant_account_id) DO NOTHING;

INSERT INTO parties
(party_id, organisation, organisation_name, surname, forenames, title)
VALUES (262902, 'N', NULL, 'PO2629', 'InactiveOnly', 'Ms')
    ON CONFLICT (party_id) DO NOTHING;

INSERT INTO defendant_account_parties
(defendant_account_party_id, defendant_account_id, party_id, association_type, debtor)
VALUES (262902, 262902, 262902, 'Defendant', 'Y')
    ON CONFLICT (defendant_account_party_id) DO NOTHING;

INSERT INTO payment_terms
(payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance)
VALUES (26290201, 262902, '2024-01-10 10:00:00', '01000000A'
       , 'B', '2026-01-01 00:00:00', 'M', 222.22, NULL
       , 20, 'N', 500.00)
    ON CONFLICT (payment_terms_id) DO NOTHING;

UPDATE payment_terms
SET active = FALSE
WHERE payment_terms_id = 26290201
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');

INSERT INTO NOTES
( note_id, note_type, associated_record_type, associated_record_id
, note_text, posted_date, posted_by)
VALUES ( 001077, 'AC', 'defendant_accounts', 0077
       , 'Comment for Notes for Ms Anna Graham', NULL, 'Dr Notes');

-- Add multiple aliases for individual party 77 (Anna Graham) to test aliases array
INSERT INTO aliases
(alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES 
(7701, 77, 'Smith', 'Annie', 1, NULL),
(7702, 77, 'Johnson', 'Anne', 2, NULL),
(7703, 77, 'Williams', 'Ana', 3, NULL);

-- 177B (inactive) — new unique ID
INSERT INTO defendant_accounts (
  defendant_account_id, business_unit_id, account_number,
  imposed_hearing_date, imposing_court_id, amount_imposed, amount_paid, account_balance,
  account_status, completed_date, enforcing_court_id, last_hearing_court_id, last_hearing_date,
  last_movement_date, last_changed_date, last_enforcement,
  originator_name, originator_id, originator_type,
  allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period,
  enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id,
  unit_fine_detail, unit_fine_value, collection_order, collection_order_date,
  further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date,
  consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by,
  prosecutor_case_reference, enforcement_case_status, account_type,
  account_comments, account_note_1, account_note_2, account_note_3, version_number
)
VALUES (
  9077, 78, '177B',
  '2023-11-03 16:05:10', 780000000185, 700.58, 700.58, 0.00,           -- balance 0 => inactive
  'C', '2024-02-01 00:00:00', 780000000185, 780000000185, '2024-01-04 18:06:11',
  '2024-01-02 17:08:09', '2024-01-03 12:00:12', '10',
  'Seed data', NULL, NULL,
  'N', 'N', 14, 21,
  'FWEC', 780000000021, 240,
  'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00',
  '2023-12-19 00:00:00', NULL, NULL, NULL,
  'Y', 'Y', '2024-01-01 00:00:00', '11111111A',
  '090B', NULL, 'Fine',
  'Text - Account Comment 177B', 'free_text_note_1', 'free_text_note_2', 'free_text_note_3', 1
);

INSERT INTO defendant_account_parties
( defendant_account_party_id, defendant_account_id, party_id, association_type, debtor )
VALUES ( 9077, 9077, 77, 'Defendant', 'Y' );

--  NEW: Record with business_unit_id = 78, to test partially populated party (all nulls)
INSERT INTO defendant_accounts
(defendant_account_id, version_number, business_unit_id, account_number,
 amount_paid, account_balance, amount_imposed, account_status,
 prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type, collection_order, payment_card_requested)
VALUES (88, 0, 78, '188A',
        100.00, 400.00, 500.00, 'L',
        '188PCR', 'N', 'N', 'Fine', 'N', 'N');


--  NEW: Party record with all null personal fields (to test null field mapping and alias fallback)
INSERT INTO parties
(party_id, organisation, organisation_name,
 surname, forenames, title,
 address_line_1, postcode, birth_date, national_insurance_number)
VALUES (88, 'N', NULL,
        NULL, NULL, NULL,
        NULL, NULL, NULL, NULL);

INSERT INTO defendant_account_parties
(defendant_account_party_id, defendant_account_id, party_id,
 association_type, debtor)
VALUES (88, 88, 88,
        'Defendant', 'Y');

--  NEW: Alias added for party 88 (to test alias fallback mapping logic)
INSERT INTO aliases
(alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES (8801, 88, 'AliasSurname', 'AliasForenames', 1, 'AliasOrg');

-- ✅ TEST DATA: Account with both main name and alias (used for alias match when main is present)
-- Purpose: Ensure that alias match works even if party also has non-null surname/forenames

INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (901, 0, 78, '901A',
        100.00, 400.00, 500.00, 'L',
        '901PCR', 'N', 'N', 'Fine',
        'N', 'N');

INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title,
                     address_line_1, postcode, birth_date, national_insurance_number)
VALUES (901, 'N', NULL,
        'MainSurname', 'MainForenames', 'Mr',
        'Alias Street', 'AL1 1AS', '1980-01-01', 'XX999999X');

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (901, 901, 901,
        'Defendant', 'Y');

INSERT INTO aliases (alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES
    (9011, 901, 'AliasSurname1', 'AliasForenames1', 1, 'AliasOrg1'),
    (9012, 901, 'AliasSurname2', 'AliasForenames2', 2, 'AliasOrg2'),
    (9013, 901, 'AliasSurname3', 'AliasForenames3', 3, 'AliasOrg3'),
    (9014, 901, 'AliasSurname4', 'AliasForenames4', 4, 'AliasOrg4'),
    (9015, 901, 'AliasSurname5', 'AliasForenames5', 5, 'AliasOrg5');

-- Dummy business unit to satisfy FK constraint but trigger Hibernate fallback
INSERT INTO business_units (business_unit_id,
                            business_unit_name,
                            business_unit_type)
VALUES (9999,
        '', -- Empty name to simulate "missing"
        'INVALID' -- Invalid type (must not match enum/expected values)
       );

--  Test record with non-existent business_unit_id to test fallback logic (getBusinessUnit() == null)
INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (999, 0, 9999, '199A',
        100.00, 400.00, 500.00, 'L',
        '199PCR', 'N', 'N', 'Fine',
        'N', 'N');

-- Party + link
INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title)
VALUES (999, 'N', NULL,
        'Fallback', 'NullBU', 'Mr');

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (999, 999, 999,
        'Defendant', 'Y');

-- Debug/inspection row: dedicated account+party with distinctive link id (77444)
-- Placed under BU 9999 to avoid influencing BU=78 count-based tests
INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (77444, 0, 9999, '77444A',
        0.00, 0.00, 0.00, 'L',
        '77444PCR', 'N', 'N', 'Fine',
        'N', 'N')
ON CONFLICT (defendant_account_id) DO NOTHING;

INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title)
VALUES (77444, 'N', NULL,
        'Debug', 'SevenSevenFourFour', 'Mr')
ON CONFLICT (party_id) DO NOTHING;

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (77444, 77444, 77444, 'Defendant', 'Y')
ON CONFLICT (defendant_account_party_id) DO NOTHING;

-- Organisation party for Sainsco (used in organisation-only search tests)
INSERT INTO parties (party_id, organisation, organisation_name)
VALUES (333, 'Y', 'Sainsco');

INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (333, 0, 78, '333A',
        100.00, 100.00, 200.00, 'L',
        '333PCR', 'N', 'N', 'Fine',
        'N', 'N');

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (333, 333, 333,
        'Defendant', 'Y');

-- Complete organisation with address details for AC9 multi-parameter testing
INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (555, 0, 78, '555O',
        250.00, 750.00, 1000.00, 'L',
        '555PCR', 'N', 'N', 'Fine',
        'N', 'N');

INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title,
                     address_line_1, address_line_2, address_line_3,
                     address_line_4, address_line_5, postcode,
                     account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES (555, 'Y', 'TechCorp Solutions Ltd',
        NULL, NULL, NULL,
        'Business Park', '42 Innovation Drive', 'Tech District',
        'Birmingham', NULL, 'B15 3TG',
        'Debtor', NULL, NULL, NULL, NULL);

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (555, 555, 555,
        'Defendant', 'Y');

-- Company in different business unit for AC9a business unit filtering test
INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (666, 0, 9999, '666C',
        150.00, 850.00, 1000.00, 'L',
        '666PCR', 'N', 'N', 'Fine',
        'N', 'N');

INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title,
                     address_line_1, address_line_2, address_line_3,
                     address_line_4, address_line_5, postcode,
                     account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES (666, 'Y', 'TechCorp Global Ltd',
        NULL, NULL, NULL,
        'Corporate Plaza', '15 Finance Street', 'Business Quarter',
        'Manchester', NULL, 'M1 4BD',
        'Debtor', NULL, NULL, NULL, NULL);

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (666, 666, 666,
        'Defendant', 'Y');

-- Completed company account for AC9b active account filtering test
INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                completed_date, prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (777, 0, 78, '777CC',
        500.00, 0.00, 500.00, 'C',
        '2024-01-15 10:00:00', '777PCR', 'N', 'N', 'Fine',
        'N', 'N');

INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title,
                     address_line_1, address_line_2, address_line_3,
                     address_line_4, address_line_5, postcode,
                     account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES (777, 'Y', 'TechCorp Completed Ltd',
        NULL, NULL, NULL,
        'Completed Business Park', '99 Finished Drive', 'Final District',
        'London', NULL, 'EC1A 1BB',
        'Debtor', NULL, NULL, NULL, NULL);

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (777, 777, 777,
        'Defendant', 'Y');

-- Add a completed (inactive) account for testing active_accounts_only filtering
INSERT INTO defendant_accounts (defendant_account_id, version_number, business_unit_id, account_number,
                                imposed_hearing_date, imposing_court_id, amount_imposed,
                                amount_paid, account_balance, account_status,
                                completed_date, enforcing_court_id, last_hearing_court_id, last_hearing_date,
                                last_movement_date, last_changed_date, last_enforcement,
                                originator_name, originator_id, originator_type,
                                allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period,
                                enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id,
                                unit_fine_detail, unit_fine_value, collection_order, collection_order_date,
                                further_steps_notice_date, confiscation_order_date, fine_registration_date,
                                suspended_committal_date,
                                consolidated_account_type, payment_card_requested, payment_card_requested_date,
                                payment_card_requested_by,
                                prosecutor_case_reference, enforcement_case_status, account_type)
VALUES (444, 0, 78, '444C',
        '2023-10-15 14:30:00', 780000000185, 300.00,
        300.00, 0.00, 'C',
        '2024-02-15 10:00:00', 780000000185, 780000000185, '2024-02-14 16:00:00',
        '2024-02-15 10:00:00', '2024-02-15 10:00:00', 'PAID',
        'Magistrates Court', NULL, NULL,
        'N', 'N', 14, 21,
        NULL, NULL, NULL,
        'GB pound sterling', 300.00, 'Y', '2023-10-20 00:00:00',
        '2023-10-21 00:00:00', NULL, NULL, NULL,
        'Y', 'N', NULL, NULL,
        '444PCR', NULL, 'Fine');

INSERT INTO parties (party_id, organisation, organisation_name,
                     surname, forenames, title,
                     address_line_1, address_line_2, address_line_3,
                     address_line_4, address_line_5, postcode,
                     account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES (444, 'N', NULL,
        'Graham', 'Robert', 'Mr',
        'High Street', '123 Main Road', 'London',
        NULL, NULL, 'SW1A 1AA',
        'Debtor', '1975-05-15 00:00:00', 48, 'B22222B', NULL);

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (444, 444, 444,
        'Defendant', 'Y');

-- Company aliases for AC9d/AC9di testing (inserted after all parties exist)
INSERT INTO aliases (alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES
-- Aliases for TechCorp Solutions Ltd (party 555)
(5551, 555, NULL, NULL, 1, 'TechCorp Ltd'),
(5552, 555, NULL, NULL, 2, 'TC Solutions Limited'),
-- Aliases for TechCorp Global Ltd (party 666)
(6661, 666, NULL, NULL, 1, 'TechCorp Global Limited'),
(6662, 666, NULL, NULL, 2, 'TC Global Ltd'),
-- Aliases for TechCorp Completed Ltd (party 777)
(7771, 777, NULL, NULL, 1, 'TechCorp Completed Limited');

-- ✅ TEST DATA: Account where party is an organisation

INSERT INTO defendant_accounts
( defendant_account_id, business_unit_id, account_number
, imposed_hearing_date, imposing_court_id, amount_imposed
, amount_paid, account_balance, account_status, completed_date
, enforcing_court_id, last_hearing_court_id, last_hearing_date
, last_movement_date, last_changed_date, last_enforcement
, originator_name, originator_id, originator_type
, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
, unit_fine_detail, unit_fine_value, collection_order, collection_order_date
, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
, prosecutor_case_reference, enforcement_case_status, account_type
, account_comments, account_note_1, account_note_2, account_note_3
, version_number)
VALUES ( 10001, 078, '10001A'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-01-04 18:06:11'
       , '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'REM'
       , 'Brentwood Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00'
       , '2023-12-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2024-01-01 00:00:00', '11111111A'
       , 'REF100001', NULL, 'Fine'
       , 'Text - Account Comment', 'free_text_note_1', 'free_text_note_2', 'free_text_note_3'
       , 1);

INSERT INTO parties
( party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES ( 10001, 'Y', 'Kings Arms'
       , 'McNamara', 'Peter', 'Ms'
       , 'Regent Court', '10001 Sydney Road', 'Bournemouth, Dorset'
       , NULL, NULL, 'BH3 2AG'
       , 'Debtor', '1980-02-03 00:00:00', 33, 'A11111A', NULL);


INSERT INTO defendant_account_parties
(defendant_account_party_id, defendant_account_id, party_id,
 association_type, debtor)
VALUES (10001, 10001, 10001,
        'Defendant', 'Y');

INSERT INTO aliases
(alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES 
(100011, 10001, 'AliasSurname', 'AliasForenames', 1, 'AliasOrg'),
(100012, 10001, 'SecondAlias', 'SecondForenames', 2, 'SecondAliasOrg'),
(100013, 10001, 'ThirdAlias', 'ThirdForenames', 3, 'ThirdAliasOrg');

INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_address_line_2,
  employer_address_line_3, employer_address_line_4, employer_address_line_5,
  employer_postcode, employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
( 10001, 'Toyota Prius', 'XY11ZAB',
  'Yellow King', '99 Employer Road', NULL,
  NULL, NULL, NULL,
  'EMP1 2AA', 'EMPREF10001', '02079997777', 'employer10001@company.com',
  'EN', NULL, 'EN', NULL );

INSERT INTO payment_terms
( payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance)
VALUES ( 10001, 10001, '2023-11-03 16:05:10', '01000000A'
       , 'B', '2025-10-12 00:00:00', 'W', NULL, NULL
       , 120, 'N', 700.58);

UPDATE payment_terms
SET active = TRUE
WHERE payment_terms_id = 10001
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');

-- ✅ END TEST DATA: Account where party is an organisation

-- ✅ TEST DATA: Account where party is an organisation
--                 both language preferences are not set (null)
--                 account comments and notes are null

INSERT INTO defendant_accounts
( defendant_account_id, business_unit_id, account_number
, imposed_hearing_date, imposing_court_id, amount_imposed
, amount_paid, account_balance, account_status, completed_date
, enforcing_court_id, last_hearing_court_id, last_hearing_date
, last_movement_date, last_changed_date, last_enforcement
, originator_name, originator_id, originator_type
, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
, unit_fine_detail, unit_fine_value, collection_order, collection_order_date
, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
, prosecutor_case_reference, enforcement_case_status, account_type
, account_comments, account_note_1, account_note_2, account_note_3
, version_number)
VALUES ( 10002, 078, '10002A'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-01-04 18:06:11'
       , '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'REM'
       , 'Brentwood Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00'
       , '2023-12-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2024-01-01 00:00:00', '11111111A'
       , 'REF100001', NULL, 'Fine'
       , NULL, NULL, NULL, NULL
       , 1);

INSERT INTO parties
( party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES ( 10002, 'Y', 'Kings Arms'
       , 'McNamara', 'Peter', 'Ms'
       , 'Regent Court', '10001 Sydney Road', 'Bournemouth, Dorset'
       , NULL, NULL, 'BH3 2AG'
       , 'Debtor', '1980-02-03 00:00:00', 33, 'A11111A', NULL);


INSERT INTO defendant_account_parties
(defendant_account_party_id, defendant_account_id, party_id,
 association_type, debtor)
VALUES (10002, 10002, 10002,
        'Defendant', 'Y');

INSERT INTO aliases
(alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES (10002, 10002, 'AliasSurname', 'AliasForenames', 1, 'AliasOrg');

INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_address_line_2,
  employer_address_line_3, employer_address_line_4, employer_address_line_5,
  employer_postcode, employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
( 10002, 'Toyota Prius', 'XY11ZAB',
  'Yellow King', '99 Employer Road', NULL,
  NULL, NULL, NULL,
  'EMP1 2AA', 'EMPREF10001', '02079997777', 'employer10001@company.com',
  NULL, NULL, NULL, NULL );

INSERT INTO payment_terms
( payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance)
VALUES ( 10002, 10002, '2023-11-03 16:05:10', '01000000A'
       , 'B', '2025-10-12 00:00:00', 'W', NULL, NULL
       , 120, 'N', 700.58);

UPDATE payment_terms
SET active = TRUE
WHERE payment_terms_id = 10002
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');

-- ✅ END TEST DATA: Account where party is an organisation
--                     both language preferences are not set (null)

-- ✅ TEST DATA: Account where party is an organisation
--                 one of language preferences is not set (null)

INSERT INTO defendant_accounts
( defendant_account_id, business_unit_id, account_number
, imposed_hearing_date, imposing_court_id, amount_imposed
, amount_paid, account_balance, account_status, completed_date
, enforcing_court_id, last_hearing_court_id, last_hearing_date
, last_movement_date, last_changed_date, last_enforcement
, originator_name, originator_id, originator_type
, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
, unit_fine_detail, unit_fine_value, collection_order, collection_order_date
, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
, prosecutor_case_reference, enforcement_case_status, account_type
, account_comments, account_note_1, account_note_2, account_note_3
, version_number)
VALUES ( 10003, 078, '10003A'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-01-04 18:06:11'
       , '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'REM'
       , 'Brentwood Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00'
       , '2023-12-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2024-01-01 00:00:00', '11111111A'
       , 'REF100001', NULL, 'Fine'
       , 'Text - Account Comment', NULL, 'free_text_note_2', 'free_text_note_3'
       , 1);

INSERT INTO parties
( party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES ( 10003, 'Y', 'Kings Arms'
       , 'McNamara', 'Peter', 'Ms'
       , 'Regent Court', '10001 Sydney Road', 'Bournemouth, Dorset'
       , NULL, NULL, 'BH3 2AG'
       , 'Debtor', '1980-02-03 00:00:00', 33, 'A11111A', NULL);


INSERT INTO defendant_account_parties
(defendant_account_party_id, defendant_account_id, party_id,
 association_type, debtor)
VALUES (10003, 10003, 10003,
        'Defendant', 'Y');

INSERT INTO aliases
(alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES (10003, 10003, 'AliasSurname', 'AliasForenames', 1, 'AliasOrg');

INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_address_line_2,
  employer_address_line_3, employer_address_line_4, employer_address_line_5,
  employer_postcode, employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
( 10003, 'Toyota Prius', 'XY11ZAB',
  'Yellow King', '99 Employer Road', NULL,
  NULL, NULL, NULL,
  'EMP1 2AA', 'EMPREF10001', '02079997777', 'employer10001@company.com',
  'EN', NULL, NULL, NULL );

INSERT INTO payment_terms
( payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance)
VALUES ( 10003, 10003, '2023-11-03 16:05:10', '01000000A'
       , 'B', '2025-10-12 00:00:00', 'W', NULL, NULL
       , 120, 'N', 700.58);

UPDATE payment_terms
SET active = TRUE
WHERE payment_terms_id = 10003
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');

-- ✅ END TEST DATA: Account where party is an organisation
--                     one of language preferences is not set (null)

-- ✅ TEST DATA: Account where party is an individual (parent/guardian)

INSERT INTO defendant_accounts
( defendant_account_id, business_unit_id, account_number
, imposed_hearing_date, imposing_court_id, amount_imposed
, amount_paid, account_balance, account_status, completed_date
, enforcing_court_id, last_hearing_court_id, last_hearing_date
, last_movement_date, last_changed_date, last_enforcement
, originator_name, originator_id, originator_type
, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
, unit_fine_detail, unit_fine_value, collection_order, collection_order_date
, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
, prosecutor_case_reference, enforcement_case_status, account_type
, account_comments, account_note_1, account_note_2, account_note_3
, version_number)
VALUES ( 10004, 078, '10004A'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-01-04 18:06:11'
       , '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'REM'
       , 'Kingston-upon-Thames Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00'
       , '2023-12-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2024-01-01 00:00:00', '11111111A'
       , 'PRREF10004', NULL, 'Fine'
       , NULL, NULL, NULL, NULL
       , 1);

INSERT INTO parties
( party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES ( 10004, 'N', 'Sainsco'
       , 'Gallagher', 'Eduardo', 'Mr'
       , 'Round House', '123 Holdenhurst Road', 'Poole, Dorset'
       , NULL, NULL, 'BH13 1PO'
       , 'Debtor', '1980-02-03 00:00:00', 33, 'NI1111C', NULL);


INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES ( 10004, 10004, 10004,
        'Parent/Guardian', 'Y');

INSERT INTO aliases
(alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES (10004, 10004, 'AliasSurname1', 'AliasForenames1', 1, NULL);

INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_address_line_2,
  employer_address_line_3, employer_address_line_4, employer_address_line_5,
  employer_postcode, employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
( 10004, 'XPENG G7', 'XP21JZP',
  'The Ladugrove', '104 Employer Road', NULL,
  NULL, NULL, NULL,
  'EMP1 2AA', 'EMPREF10004', '02079997777', 'employer10004@company.com',
  'EN', NULL, NULL, NULL );

INSERT INTO payment_terms
( payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance)
VALUES ( 10004, 10004, '2024-11-03 16:05:10', '01000000A'
       , 'B', '2025-10-12 00:00:00', 'W', 200.00, 700
       , 120, 'N', 700.58);

UPDATE payment_terms
SET active = TRUE
WHERE payment_terms_id = 10004
  AND EXISTS (SELECT 1
              FROM information_schema.columns
              WHERE table_schema = 'public'
                AND table_name = 'payment_terms'
                AND column_name = 'active');

-- ✅ END TEST DATA: Account where party is an individual (parent/guardian)

INSERT INTO defendant_accounts
( defendant_account_id, version_number, business_unit_id, account_number,
  amount_paid, account_balance, amount_imposed, account_status,
  prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
  collection_order, payment_card_requested )
VALUES (20010, 0, 78, '20010A',
        0.00, 0.00, 0.00, 'L',
        '20010PCR', 'N', 'N', 'Fine',
        'N', 'N')
ON CONFLICT (defendant_account_id) DO NOTHING;

-- Party linked to the account (starts as organisation=false → your PUT can set org=true if you like)
INSERT INTO parties
( party_id, organisation, organisation_name,
  surname, forenames, title,
  address_line_1, postcode, birth_date, national_insurance_number, last_changed_date )
VALUES (20010, 'N', NULL,
        'SeedSurname', 'SeedForenames', 'Mr',
        'Seed Address', 'SE1 1ED', '1980-01-01 00:00:00', 'SEEDNI10', NULL)
ON CONFLICT (party_id) DO NOTHING;

-- DAP linking account ↔ party; association_type kept simple
INSERT INTO defendant_account_parties
( defendant_account_party_id, defendant_account_id, party_id, association_type, debtor )
VALUES (20010, 20010, 20010, 'Defendant', 'Y')
ON CONFLICT (defendant_account_party_id) DO NOTHING;

-- Minimal debtor_detail row so the upsert/patch has something to merge with
INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_postcode,
  employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
    ( 20010, NULL, NULL,
      NULL, NULL, NULL,
      NULL, NULL, NULL,
      NULL, NULL, NULL, NULL )
ON CONFLICT (party_id) DO NOTHING;

INSERT INTO aliases
(alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES
    (200101, 20010, NULL, NULL, 1, 'Seed Org Alias 1'),
    (200102, 20010, NULL, NULL, 2, 'Seed Org Alias 2')
ON CONFLICT (alias_id) DO NOTHING;

-- === Individual aliases test dataset (isolated) ===
-- Account 22004 (BU 78)
INSERT INTO defendant_accounts
( defendant_account_id, version_number, business_unit_id, account_number,
  amount_paid, account_balance, amount_imposed, account_status,
  prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
  collection_order, payment_card_requested )
VALUES (22004, 0, 78, '22004A',
        0.00, 0.00, 0.00, 'L',
        '22004PCR', 'N', 'N', 'Fine',
        'N', 'N')
ON CONFLICT (defendant_account_id) DO NOTHING;

-- Party 22004 (individual)
INSERT INTO parties
( party_id, organisation, organisation_name,
  surname, forenames, title,
  address_line_1, postcode, birth_date, national_insurance_number, last_changed_date )
VALUES (22004, 'N', NULL,
        'SeedSurname22004', 'SeedForenames22004', 'Mr',
        'Seed Address 22004', 'SE2 0AA', '1990-01-01 00:00:00', 'SNI22004', NULL)
ON CONFLICT (party_id) DO NOTHING;

-- DAP link
INSERT INTO defendant_account_parties
( defendant_account_party_id, defendant_account_id, party_id, association_type, debtor )
VALUES (22004, 22004, 22004, 'Defendant', 'Y')
ON CONFLICT (defendant_account_party_id) DO NOTHING;

-- Minimal debtor_detail
INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_postcode,
  employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
    ( 22004, NULL, NULL,
      NULL, NULL, NULL,
      NULL, NULL, NULL,
      NULL, NULL, NULL, NULL )
ON CONFLICT (party_id) DO NOTHING;

-- Seed one individual alias we will update in the test
INSERT INTO aliases (alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES (2200401, 22004, 'AliasSurnameSeed', 'AliasForenamesSeed', 1, NULL)
ON CONFLICT (alias_id) DO NOTHING;

-- Populate with Enforcement
INSERT INTO enforcements
( enforcement_id, defendant_account_id, posted_date, posted_by,
  result_id, reason, enforcer_id, jail_days, warrant_reference,
  result_responses)
VALUES
(
  10001, 78, '2025-02-23 16:05:10', 'Merlin',
  'FEES', 'Late Payment', 21, 101, 'Warrent007',
 NULL
),
(
    10002, 78, '2025-02-24 12:05:10', 'Merlin',
    'FCOST', 'Late Payment', 21, 101, 'Warrent007',
 NULL
),
(
    10003, 78, '2025-02-13 10:05:10', 'Merlin',
    'FCOST', 'Late Payment', 21, 101, 'Warrent007',
 NULL
),
(
    10004, 78, '2025-02-13 10:05:10', 'Merlin',
    'MPSO', 'Late Payment', 21, 101, 'Warrent007',
    '{"reason":"Evasion of Prison", "supervisor":"Mordred"}'
);

INSERT INTO defendant_accounts
( defendant_account_id, version_number, business_unit_id, account_number
, imposed_hearing_date, imposing_court_id, amount_imposed
, amount_paid, account_balance, account_status, completed_date
, enforcing_court_id, last_hearing_court_id, last_hearing_date
, last_movement_date, last_changed_date, last_enforcement
, originator_name, originator_id, originator_type
, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
, unit_fine_detail, unit_fine_value, collection_order, collection_order_date
, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
, prosecutor_case_reference, enforcement_case_status, account_type
, account_comments, account_note_1, account_note_2, account_note_3
, jail_days
)
VALUES ( 991199, 0, 78, '1989'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-01-04 18:06:11'
       , '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'ABDC'
       , 'Kingston-upon-Thames Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00'
       , '2023-12-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2024-01-01 00:00:00', '11111111A'
       , '090B', NULL, 'Fine'
       , 'Text - Account Comment', 'free_text_note_1', 'free_text_note_2', 'free_text_note_3'
       , 0
       );

INSERT INTO parties
( party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES ( 991199, 'N', 'Sainsco'
       , 'Surnamey', 'Forenamey', 'Mr'
       , 'Square House', '123 Holdenhurst Close', 'Poole, Dorset'
       , NULL, NULL, 'BH13 1PO'
       , 'Debtor', '1980-02-03 00:00:00', 33, 'NI2221C', NULL);


INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES ( 991199, 991199, 991199,
         'Defendant', 'Y');

INSERT INTO defendant_accounts
( defendant_account_id, version_number, business_unit_id, account_number
, imposed_hearing_date, imposing_court_id, amount_imposed
, amount_paid, account_balance, account_status, completed_date
, enforcing_court_id, last_hearing_court_id, last_hearing_date
, last_movement_date, last_changed_date, last_enforcement
, originator_name, originator_id, originator_type
, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
, unit_fine_detail, unit_fine_value, collection_order, collection_order_date
, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
, prosecutor_case_reference, enforcement_case_status, account_type
, account_comments, account_note_1, account_note_2, account_note_3
, jail_days
)
VALUES ( 991198, 0, 78, '1988'
       , '2023-11-03 16:05:10', 780000000185, 700.58
       , 200.00, 500.58, 'L', NULL
       , 780000000185, 780000000185, '2024-01-04 18:06:11'
       , '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'ABDC'
       , 'Kingston-upon-Thames Mags Court', NULL, NULL
       , 'N', 'N', 14, 21
       , 'FWEC', 780000000021, 240
       , 'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00'
       , '2023-12-19 00:00:00', NULL, NULL, NULL
       , 'Y', 'Y', '2024-01-01 00:00:00', '11111111A'
       , '090B', NULL, 'Fine'
       , 'Text - Account Comment', 'free_text_note_1', 'free_text_note_2', 'free_text_note_3'
       , 1
       );

INSERT INTO parties
( party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date)
VALUES ( 991198, 'N', 'Sainsco'
       , 'Surnamey', 'Forenamey', 'Mr'
       , 'Square House', '123 Holdenhurst Close', 'Poole, Dorset'
       , NULL, NULL, 'BH13 1PO'
       , 'Debtor', '1980-02-03 00:00:00', 33, 'NI2221C', NULL);


INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES ( 991198, 991198, 991198,
         'Defendant', 'Y');
