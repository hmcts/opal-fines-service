/**
* OPAL Program
*
* MODULE      : add_major_creditor_history_dev_data.sql
*
* DESCRIPTION : Adds dev major creditor account history data for continuous scroll testing.
*
* VERSION HISTORY:
*
* Date          Author    Version     Nature of Change
* ----------    ------    --------    ----------------------------------------------------------------------------
* 05/08/2026    C Cho     1.0         PO-9150 - Add major creditor account history dev test data.
*
**/

INSERT INTO public.major_creditors (
  major_creditor_id, business_unit_id, major_creditor_code, name,
  address_line_1, address_line_2, address_line_3, postcode,
  contact_name, contact_telephone, contact_email
)
VALUES (
  99000000000950, 77, 'TEST', 'PO-9150 Major Creditor',
  '1 Test Data House', 'Birmingham', 'West Midlands', 'B1 1AA',
  NULL, NULL, NULL
);

INSERT INTO public.creditor_accounts (
  creditor_account_id, business_unit_id, account_number, creditor_account_type,
  prosecution_service, major_creditor_id, minor_creditor_party_id,
  repayment, hold_payout, pay_by_bacs, bank_sort_code,
  bank_account_number, bank_account_name, bank_account_reference,
  bank_account_type, last_changed_date, version_number
)
VALUES (
  99000000000850, 77, 'PO9150TEST', 'MJ',
  false, 99000000000950, NULL,
  false, false, true, '123456',
  '12345678', '9150 Test Account', 'PO9150TEST',
  '1', NULL, 1
);

INSERT INTO public.creditor_transactions (
  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
  transaction_type, transaction_amount, imposition_result_id, payment_processed,
  payment_reference, status, status_date, associated_record_type, associated_record_id
)
VALUES
  (99000000006021, 99000000000850, '2026-06-20 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 7.50, 'ABDC', true, 'MJH0000001', 'C', '2026-06-20 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006022, 99000000000850, '2026-06-19 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 15.00, 'ABDC', true, 'MJH0000002', 'C', '2026-06-19 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006023, 99000000000850, '2026-06-18 09:00:00', 'L077HT', 'Test User',
   'CHEQUE', 22.50, 'ABDC', true, 'MJH0000003', 'C', '2026-06-18 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006024, 99000000000850, '2026-06-17 09:00:00', 'L077HT', 'Test User',
   'BACS', 30.00, 'ABDC', true, 'MJH0000004', 'C', '2026-06-17 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006025, 99000000000850, '2026-06-16 09:00:00', 'L077HT', 'Test User',
   'MADJ', 37.50, 'ABDC', true, 'MJH0000005', 'C', '2026-06-16 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006026, 99000000000850, '2026-06-15 09:00:00', 'L077HT', 'Test User',
   'CHEQUE', 45.00, 'ABDC', true, 'MJH0000006', 'P', '2026-06-15 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006027, 99000000000850, '2026-06-14 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 52.50, 'ABDC', true, 'MJH0000007', 'C', '2026-06-14 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006028, 99000000000850, '2026-06-13 09:00:00', 'L077HT', 'Test User',
   'BACS', 60.00, 'ABDC', true, 'MJH0000008', 'C', '2026-06-13 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006029, 99000000000850, '2026-06-12 09:00:00', 'L077HT', 'Test User',
   'CHEQUE', 67.50, 'ABDC', true, 'MJH0000009', 'C', '2026-06-12 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006030, 99000000000850, '2026-06-11 09:00:00', 'L077HT', 'Test User',
   'MADJ', 75.00, 'ABDC', true, 'MJH0000010', 'C', '2026-06-11 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006031, 99000000000850, '2026-06-10 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 82.50, 'ABDC', true, 'MJH0000011', 'C', '2026-06-10 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006032, 99000000000850, '2026-06-09 09:00:00', 'L077HT', 'Test User',
   'BACS', 90.00, 'ABDC', true, 'MJH0000012', 'P', '2026-06-09 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006033, 99000000000850, '2026-06-08 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 97.50, 'ABDC', true, 'MJH0000013', 'C', '2026-06-08 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006034, 99000000000850, '2026-06-07 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 105.00, 'ABDC', true, 'MJH0000014', 'C', '2026-06-07 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006035, 99000000000850, '2026-06-06 09:00:00', 'L077HT', 'Test User',
   'MADJ', 112.50, 'ABDC', true, 'MJH0000015', 'C', '2026-06-06 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006036, 99000000000850, '2026-06-05 09:00:00', 'L077HT', 'Test User',
   'BACS', 120.00, 'ABDC', true, 'MJH0000016', 'C', '2026-06-05 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006037, 99000000000850, '2026-06-04 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 127.50, 'ABDC', true, 'MJH0000017', 'C', '2026-06-04 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006038, 99000000000850, '2026-06-03 09:00:00', 'L077HT', 'Test User',
   'CHEQUE', 135.00, 'ABDC', true, 'MJH0000018', 'P', '2026-06-03 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006039, 99000000000850, '2026-06-02 09:00:00', 'L077HT', 'Test User',
   'PAYMNT', 142.50, 'ABDC', true, 'MJH0000019', 'C', '2026-06-02 09:00:00',
   'creditor_accounts', '99000000000850'),
  (99000000006040, 99000000000850, '2026-06-01 09:00:00', 'L077HT', 'Test User',
   'MADJ', 150.00, 'ABDC', true, 'MJH0000020', 'C', '2026-06-01 09:00:00',
   'creditor_accounts', '99000000000850');

SELECT setval('public.major_creditor_id_seq', 99000000000950, true);
SELECT setval('public.creditor_account_id_seq', 99000000000850, true);
SELECT setval('public.creditor_transaction_id_seq', 99000000006040, true);
