/**
* OPAL Program
*
* MODULE      : insert_into_minor_creditors.sql
*
* DESCRIPTION : Inserts rows of data into the minor_creditor tables for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------------
* 18/08/2025  M Mollins   1.0      PO-713 Inserts rows of data into the MINOR_CREDITORS tables.
*/

-- Insert into parties
INSERT INTO public.parties (
  party_id, organisation, organisation_name,
  surname, forenames, title,
  address_line_1, address_line_2, address_line_3,
  address_line_4, address_line_5, postcode,
  account_type, birth_date, age, national_insurance_number, last_changed_date
)
VALUES
  -- Defendant (individual)
  (77, 'N', NULL,
   'Graham', 'Anna', 'Ms',
   'Lumber House', '77 Gordon Road', 'Maidstone, Kent',
   NULL, NULL, 'MA4 1AL',
   'Debtor', '1980-02-03 00:00:00', 45, 'A11111A', NULL),

  -- Creditor (organisation)
  (9000, 'Y', 'Acme Supplies Ltd',
   NULL, NULL, NULL,
   'Acme House', '1 Industrial Park', 'Maidstone, Kent',
   NULL, NULL, 'MA4 1AL',
   'Creditor', NULL, NULL, NULL, NULL);

-- Insert into creditor_accounts
INSERT INTO public.creditor_accounts (
  creditor_account_id, business_unit_id, account_number,
  creditor_account_type, prosecution_service, major_creditor_id,
  minor_creditor_party_id, from_suspense, hold_payout, pay_by_bacs,
  bank_sort_code, bank_account_number, bank_account_name,
  bank_account_reference, bank_account_type, last_changed_date
)
VALUES (
  104,                -- creditor_account_id
  10,                 -- using existing business_unit_id
  'ACC9000',          -- account_number
  'MJ',            -- creditor_account_type
  true,              -- prosecution_service
  NULL,               -- major_creditor_id
  9000,               -- minor_creditor_party_id
  FALSE,              -- from_suspense
  FALSE,              -- hold_payout
  TRUE,               -- pay_by_bacs
  '123456',         -- bank_sort_code
  '12345678',         -- bank_account_number
  'Acme Supplies Ltd',-- bank_account_name
  'ACME123REF',       -- bank_account_reference
  '1',         -- bank_account_type
  '2025-08-19 09:00:00' -- last_changed_date
);

-- Insert into defendant_accounts
INSERT INTO public.defendant_accounts (
  defendant_account_id, business_unit_id, account_number,
  imposed_hearing_date, imposing_court_id, amount_imposed,
  amount_paid, account_balance, account_status, completed_date,
  enforcing_court_id, last_hearing_court_id, last_hearing_date,
  last_movement_date, last_changed_date, last_enforcement,
  originator_name, originator_id, originator_type,
  allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period,
  enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id,
  unit_fine_detail, unit_fine_value, collection_order, collection_order_date,
  further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date,
  consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by,
  prosecutor_case_reference, enforcement_case_status, account_type
)
VALUES (
  77, 10, '177A',
  '2023-11-03 16:05:10', 780000000185, 700.58,
  200.00, 500.58, 'L', NULL,
  780000000185, 780000000185, '2024-01-04 18:06:11',
  '2024-01-02 17:08:09', '2024-01-03 12:00:12', 'REM',
  'Kingston-upon-Thames Mags Court', NULL, NULL,
  'N', 'N', 14, 21,
  'FWEC', 780000000021, 240,
  'GB pound sterling', 700.00, 'Y', '2023-12-18 00:00:00',
  '2023-12-19 00:00:00', NULL, NULL, NULL,
  'Y', 'Y', '2024-01-01 00:00:00', '11111111A',
  '090A', NULL, 'Fine'
);

-- Insert into impositions
INSERT INTO public.impositions (
  imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name,
  original_posted_date, result_id, imposing_court_id, imposed_date, imposed_amount,
  paid_amount, offence_id, offence_title, offence_code, creditor_account_id,
  unit_fine_adjusted, unit_fine_units, completed
)
VALUES (
  50001, 77, '2025-08-15', 102, 'M. Davies',
  '2025-08-15', 'WDN', 780000000297, '2025-08-14', 250.00,
  150.00, 54013, 'Speed', 'SP30', 104,
  FALSE, 0, FALSE
);

-- Insert into defendant_account_parties
INSERT INTO public.defendant_account_parties (
  defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
)
VALUES (
  50001, 77, 77, 'Defendant', TRUE
);

-- Insert into creditor_transactions
INSERT INTO public.creditor_transactions (
  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
  transaction_type, transaction_amount, imposition_result_id, payment_processed,
  payment_reference, status, status_date, associated_record_type, associated_record_id
)
VALUES
  (90001, 104, '2025-08-16', 102, 'M. Davies',
   'PAYMNT', 150.00, 4102, FALSE,
   'BACS', '1', '2025-08-17', 'IMPOSITION', 50001),
  (90002, 104, '2025-08-16', 102, 'M. Davies',
   'XFER', 100.00, 4103, FALSE,
   'BACS', '1', '2025-08-17', 'IMPOSITION', 50001);
