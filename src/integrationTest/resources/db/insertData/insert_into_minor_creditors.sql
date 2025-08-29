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
* ----------  ----------  -------  -------------------------------------------------------------
* 18/08/2025  M Mollins   1.0      PO-713 Inserts rows of data into the MINOR_CREDITORS tables.
*
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
  -- Creditor (set organisation = 'N' so view emits organisation=false)
  (9000, 'N', 'Acme Supplies Ltd',
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
VALUES
  (104, 10, '12345678A',
   'MJ', TRUE, NULL,
   9000, FALSE, FALSE, TRUE,
   '123456', '12345678A', 'Acme Supplies Ltd',
   'ACME123REF', '1', '2025-08-19 09:00:00'),

  -- matching 8-digit (no check letter)
  (105, 10, '12345678',
   'MJ', TRUE, NULL,
   9000, FALSE, FALSE, TRUE,
   '123456', '12345678', 'Acme Supplies Ltd',
   'ACME123REF2', '1', '2025-08-19 09:00:00');

INSERT INTO public.creditor_transactions (
  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
  transaction_type, transaction_amount, imposition_result_id, payment_processed,
  payment_reference, status, status_date, associated_record_type, associated_record_id
)
VALUES
  (90001, 104, '2025-08-16', 102, 'M. Davies',
   'PAYMNT', 150.00, NULL, FALSE,
   'BACS', '1', '2025-08-17', 'OTHER', NULL);
