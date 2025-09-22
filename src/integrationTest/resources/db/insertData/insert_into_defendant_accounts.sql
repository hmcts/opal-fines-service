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

-- Ensure BU 78 exists for joins
INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type,
                            welsh_language)
VALUES (78, 'N E Region', 'NE', 'Area', FALSE)
ON CONFLICT (business_unit_id) DO UPDATE
    SET business_unit_name = EXCLUDED.business_unit_name,
        business_unit_code = EXCLUDED.business_unit_code,
        business_unit_type = EXCLUDED.business_unit_type,
        welsh_language     = EXCLUDED.welsh_language;


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
, prosecutor_case_reference, enforcement_case_status, account_type)
VALUES ( 0077, 078, '177A'
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
       , '090A', NULL, 'Fine');

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
       , 'Debtor', '1980-02-03 00:00:00', 33, 'A11111A', NULL);

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
  'EN', NULL, 'EN', NULL );


INSERT INTO fixed_penalty_offences
(defendant_account_id, ticket_number)
VALUES (77, '888');
INSERT INTO defendant_account_parties
( defendant_account_party_id, defendant_account_id, party_id
, association_type, debtor)
VALUES ( 0077, 0077, 0077
       , 'Defendant', 'Y');

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

INSERT INTO NOTES
( note_id, note_type, associated_record_type, associated_record_id
, note_text, posted_date, posted_by)
VALUES ( 001077, 'AC', 'defendant_accounts', 0077
       , 'Comment for Notes for Ms Anna Graham', NULL, 'Dr Notes');

--  NEW: Record with business_unit_id = 78, to test partially populated party (all nulls)
INSERT INTO defendant_accounts
(defendant_account_id, business_unit_id, account_number,
 amount_paid, account_balance, amount_imposed, account_status,
 prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type, collection_order, payment_card_requested)
VALUES (88, 78, '188A',
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

INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (901, 78, '901A',
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
VALUES (9011, 901, 'AliasSurname', 'AliasForenames', 1, 'AliasOrg');

-- Dummy business unit to satisfy FK constraint but trigger Hibernate fallback
INSERT INTO business_units (business_unit_id,
                            business_unit_name,
                            business_unit_type)
VALUES (9999,
        '', -- Empty name to simulate "missing"
        'INVALID' -- Invalid type (must not match enum/expected values)
       );

--  Test record with non-existent business_unit_id to test fallback logic (getBusinessUnit() == null)
INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (999, 9999, '199A',
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

-- Organisation party for Sainsco (used in organisation-only search tests)
INSERT INTO parties (party_id, organisation, organisation_name)
VALUES (333, 'Y', 'Sainsco');

INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (333, 78, '333A',
        100.00, 100.00, 200.00, 'L',
        '333PCR', 'N', 'N', 'Fine',
        'N', 'N');

INSERT INTO defendant_account_parties (defendant_account_party_id, defendant_account_id, party_id,
                                       association_type, debtor)
VALUES (333, 333, 333,
        'Defendant', 'Y');

-- Complete organisation with address details for AC9 multi-parameter testing
INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (555, 78, '555O',
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
INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (666, 9999, '666C',
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
INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                amount_paid, account_balance, amount_imposed, account_status,
                                completed_date, prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
                                collection_order, payment_card_requested)
VALUES (777, 78, '777CC',
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
INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
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
VALUES (444, 78, '444C',
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
