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
delete from NOTES where note_id = 001001;
delete from payment_terms where payment_terms_id = 0001;
delete from defendant_account_parties where defendant_account_party_id = 0001;
delete from parties where party_id = 0001;
delete from defendant_accounts where defendant_account_id = 0001;

INSERT INTO defendant_accounts
(
defendant_account_id, business_unit_id, account_number
,imposed_hearing_date, imposing_court_id, amount_imposed
,amount_paid, account_balance, account_status, completed_date
,enforcing_court_id, last_hearing_court_id, last_hearing_date
,last_movement_date, last_changed_date, last_enforcement
,originator_name, originator_id, originator_type
,allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period
,enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id
,unit_fine_detail, unit_fine_value, collection_order, collection_order_date
,further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date
,consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by
,prosecutor_case_reference, enforcement_case_status, account_type
)
VALUES
(
0001, 078, '100A'
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
, '090A', NULL, 'Fine'
);

INSERT INTO parties
(
party_id, organisation, organisation_name
, surname, forenames, title
, address_line_1, address_line_2, address_line_3
, address_line_4, address_line_5, postcode
, account_type, birth_date, age, national_insurance_number, last_changed_date
)
VALUES
(
0001, 'N', NULL
, 'Graham', 'Anna',  'Ms'
, 'Lumber House', '54 Gordon Road', 'Maidstone, Kent'
, NULL, NULL, 'MA4 1AL'
, 'Debtor', '1980-02-03 00:00:00', 33, 'A11111A', NULL
);

INSERT INTO defendant_account_parties
(
defendant_account_party_id, defendant_account_id, party_id
, association_type, debtor
)
VALUES
(
0001, 0001, 0001
,'Defendant','Y'
);

INSERT INTO payment_terms
(
payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance
)
VALUES
(
 0001, 0001, '2023-11-03 16:05:10', '01000000A'
, 'B', '2025-10-12 00:00:00', NULL, NULL, NULL
, 120, 'N', 700.58
);

INSERT INTO NOTES
(
note_id, note_type, associated_record_type, associated_record_id
, note_text, posted_date, posted_by
)
VALUES
(
001001, 'AC', 'defendant_accounts', 0001
, 'Comment for Notes for Ms Anna Graham', NULL, 'Dr Notes'
);
