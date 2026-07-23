/**
* OPAL Program
*
* MODULE      : insert_into_po_2642_minor_creditor_history.sql
*
* DESCRIPTION : Inserts PO-2642 minor creditor history integration test data.
*
**/

INSERT INTO public.business_units (
  business_unit_id, business_unit_name, business_unit_code, business_unit_type,
  account_number_prefix, parent_business_unit_id, opal_domain, welsh_language,
  account_number_suffix
)
VALUES
  (32642, 'PO-2642 History Business Unit', 'P264', 'Area',
   'P2', NULL, 'Fines', false, 'H');

INSERT INTO public.parties (
  party_id, organisation, organisation_name,
  surname, forenames, title,
  address_line_1, address_line_2, address_line_3,
  address_line_4, address_line_5, postcode,
  account_type, birth_date, age, national_insurance_number, last_changed_date
)
VALUES
  (99264200000101, 'N', NULL,
   'History', 'Creditor', 'Mx',
   '1 Integration Street', 'History Quarter', 'Test City',
   NULL, NULL, 'PO2 6TF',
   'Creditor', NULL, NULL, NULL, NULL),
  (99264200000102, 'Y', 'PO-2642 Major Creditor',
   NULL, NULL, NULL,
   '2 Integration Street', 'History Quarter', 'Test City',
   NULL, NULL, 'PO2 6MJ',
   'Creditor', NULL, NULL, NULL, NULL);

INSERT INTO public.creditor_accounts (
  creditor_account_id, business_unit_id, account_number, creditor_account_type,
  prosecution_service, major_creditor_id, minor_creditor_party_id,
  repayment, hold_payout, pay_by_bacs,
  bank_sort_code, bank_account_number, bank_account_name, bank_account_reference,
  bank_account_type, version_number, last_changed_date
)
VALUES
  (99264200000001, 32642, 'P264MN01', 'MN',
   true, NULL, 99264200000101,
   false, false, false,
   NULL, NULL, NULL, NULL,
   NULL, 4, '2026-01-31 11:00:00'),
  (99264200000002, 32642, 'P264MJ01', 'MJ',
   true, NULL, NULL,
   false, false, false,
   NULL, NULL, NULL, NULL,
   NULL, 1, '2026-01-31 11:00:00');

INSERT INTO public.defendant_accounts (
  defendant_account_id, business_unit_id, account_number,
  amount_imposed, amount_paid, account_balance, account_status, account_type,
  version_number
)
VALUES
  (99264200001001, 32642, 'P264DEF1',
   100.00, 20.00, 80.00, 'CS', 'Fine',
   1);

INSERT INTO public.creditor_transactions (
  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
  transaction_type, transaction_amount, imposition_result_id, payment_processed,
  payment_reference, status, status_date, associated_record_type, associated_record_id
)
VALUES
  (99264200002001, 99264200000001, '2026-01-05 08:00:00', 'FINUSR1', 'Financial User One',
   'PAYMNT', 10.00, NULL, true,
   'FIN001', 'C', '2026-01-05 08:30:00', 'defendant_accounts', '99264200001001'),
  (99264200002002, 99264200000001, '2026-01-25 12:00:00', 'FINUSR2', 'Financial User Two',
   'PAYMNT', 25.00, NULL, true,
   'FIN002', 'C', '2026-01-25 12:30:00', 'defendant_accounts', '99264200001001'),
  (99264200002003, 99264200000001, '2026-01-31 10:00:00', 'FINUSR3', 'Financial User Three',
   'PAYMNT', 31.00, NULL, true,
   'FIN003', 'C', '2026-01-31 10:30:00', 'defendant_accounts', '99264200001001');

INSERT INTO public.notes (
  note_id, note_type, associated_record_type, associated_record_id,
  note_text, posted_date, posted_by, posted_by_name
)
VALUES
  (99264200003001, 'AA', 'creditor_accounts', '99264200000001',
   'Older PO-2642 note', '2026-01-10 09:00:00', 'NOTEUSR1', 'Note User One'),
  (99264200003002, 'AA', 'creditor_accounts', '99264200000001',
   'Same timestamp PO-2642 note', '2026-01-31 10:00:00', 'NOTEUSR2', 'Note User Two');

INSERT INTO public.amendments (
  amendment_id, business_unit_id, associated_record_type, associated_record_id,
  amended_date, amended_by, amended_by_name, field_code, old_value, new_value,
  case_reference, function_code
)
VALUES
  (99264200004001, 32642, 'creditor_accounts', '99264200000001',
   '2026-01-15 10:00:00', 'AMEND1', 'Amend User One', 41, 'baseline-old', 'baseline-new',
   NULL, 'minor-creditor-history'),
  (99264200004002, 32642, 'creditor_accounts', '99264200000001',
   '2026-01-31 10:00:00', 'AMEND2', 'Amend User Two', 41, 'tie-1-old', 'tie-1-new',
   NULL, 'minor-creditor-history'),
  (99264200004003, 32642, 'creditor_accounts', '99264200000001',
   '2026-01-31 10:00:00', 'AMEND3', 'Amend User Three', 41, 'tie-2-old', 'tie-2-new',
   NULL, 'minor-creditor-history');
