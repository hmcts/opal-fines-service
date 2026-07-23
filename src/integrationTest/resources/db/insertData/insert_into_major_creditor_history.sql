/**
* OPAL Program
*
* MODULE      : insert_into_major_creditor_history.sql
*
* DESCRIPTION : Inserts major creditor history integration test data.
*
**/

INSERT INTO public.business_units (
  business_unit_id, business_unit_name, business_unit_code, business_unit_type,
  account_number_prefix, parent_business_unit_id, opal_domain, welsh_language,
  account_number_suffix
)
VALUES
  (32643, 'Major Creditor History Business Unit', 'M643', 'Area',
   'M6', NULL, 'Fines', false, 'H');

INSERT INTO public.major_creditors (
  major_creditor_id, business_unit_id, major_creditor_code, name,
  address_line_1, address_line_2, address_line_3, postcode
)
VALUES
  (99264300000101, 32643, 'MJH', 'Major Creditor History',
   '1 Major Street', 'History Quarter', 'Test City', 'MJ1 1AA');

INSERT INTO public.creditor_accounts (
  creditor_account_id, business_unit_id, account_number, creditor_account_type,
  prosecution_service, major_creditor_id, minor_creditor_party_id,
  repayment, hold_payout, pay_by_bacs,
  bank_sort_code, bank_account_number, bank_account_name, bank_account_reference,
  bank_account_type, version_number, last_changed_date
)
VALUES
  (99264300000001, 32643, 'P264MJH1', 'MJ',
   true, 99264300000101, NULL,
   false, false, false,
   NULL, NULL, NULL, NULL,
   NULL, 4, '2026-01-31 11:00:00');

INSERT INTO public.creditor_transactions (
  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
  transaction_type, transaction_amount, imposition_result_id, payment_processed,
  payment_reference, status, status_date, associated_record_type, associated_record_id
)
VALUES
  (99264300002001, 99264300000001, '2026-01-05 08:00:00', 'MJUSR1', 'Major User One',
   'PAYMNT', 10.00, NULL, true,
   'MJF001', 'C', '2026-01-05 08:30:00', 'creditor_accounts', '99264300000001'),
  (99264300002002, 99264300000001, '2026-01-25 12:00:00', 'MJUSR2', 'Major User Two',
   'XFER', 25.00, NULL, true,
   'MJF002', 'P', '2026-01-25 12:30:00', 'creditor_accounts', '99264300000001'),
  (99264300002003, 99264300000001, '2026-01-31 10:00:00', 'MJUSR3', 'Major User Three',
   'MADJ', -31.00, NULL, true,
   'MJF003', 'R', '2026-01-31 10:30:00', 'creditor_accounts', '99264300000001'),
  (99264300002004, 99264300000001, '2026-01-31 10:00:00', 'MJUSR4', 'Major User Four',
   'MADJ', -4.00, NULL, true,
   'MJF004', 'R', '2026-01-31 10:45:00', 'creditor_accounts', '99264300000001');
