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
   'Creditor', NULL, NULL, NULL, NULL),
  
  -- Individual creditor for testing forenames/surname search
  (9001, 'N', NULL,
   'Smith', 'John', 'Mr',
   '123 Test Street', 'Test Area', 'Test City',
   NULL, NULL, 'TS1 2AB',
   'Creditor', NULL, NULL, NULL, NULL),
   
  -- Additional individual creditor for testing exact match - surname starts with 'Smith'
  (9002, 'N', NULL,
   'Smithson', 'Jane', 'Mrs',
   '456 Another Street', 'Another Area', 'Another City',
   NULL, NULL, 'TS3 4CD',
   'Creditor', NULL, NULL, NULL, NULL),
   
  -- Individual creditor for testing forenames exact match - forenames starts with 'John'
  (9003, 'N', NULL,
   'Doe', 'Johnathan', 'Mr',
   '789 Third Street', 'Third Area', 'Third City',
   NULL, NULL, 'TS5 6EF',
   'Creditor', NULL, NULL, NULL, NULL),
   
  -- Company creditor for testing organisation name exact match - exact match "Tech Solutions"
  (9004, 'Y', 'Tech Solutions',
   NULL, NULL, NULL,
   'Tech House', '100 Business Park', 'London',
   NULL, NULL, 'TH1 2BC',
   'Creditor', NULL, NULL, NULL, NULL),
   
  -- Company creditor for testing organisation name starts with - starts with "Tech"
  (9005, 'Y', 'Tech Solutions Ltd',
   NULL, NULL, NULL,
   'Tech Building', '200 Corporate Street', 'Birmingham',
   NULL, NULL, 'TC3 4DE',
   'Creditor', NULL, NULL, NULL, NULL),
   
  -- Company creditor for testing organisation name starts with - starts with "Tech"
  (9006, 'Y', 'Technology Partner',
   NULL, NULL, NULL,
   'Digital Centre', '300 Innovation Drive', 'Manchester',
   NULL, NULL, 'TP5 6FG',
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
   'ACME123REF2', '1', '2025-08-19 09:00:00'),

  -- Individual creditor account for John Smith
  (999950, 10, 'JS987654',
   'MJ', TRUE, NULL,
   9001, FALSE, FALSE, TRUE,
   '654321', 'JS987654', 'John Smith',
   'JOHNSMITH123', '1', '2025-08-19 09:00:00'),
   
  -- Individual creditor account for Jane Smithson  
  (999951, 10, 'JSN98765',
   'MJ', TRUE, NULL,
   9002, FALSE, FALSE, TRUE,
   '654322', 'JSN98765', 'Jane Smithson',
   'JANESMITHSON123', '1', '2025-08-19 09:00:00'),
   
  -- Individual creditor account for Jonathan Doe
  (999952, 10, 'JD123456',
   'MJ', TRUE, NULL,
   9003, FALSE, FALSE, TRUE,
   '654323', 'JD123456', 'Jonathan Doe',
   'JONATHANDOE123', '1', '2025-08-19 09:00:00'),
   
  -- Company creditor account for Tech Solutions
  (999953, 10, 'TS123456',
   'MJ', TRUE, NULL,
   9004, FALSE, FALSE, TRUE,
   '654324', 'TS123456', 'Tech Solutions',
   'TECHSOLUTIONS123', '1', '2025-08-19 09:00:00'),
   
  -- Company creditor account for Tech Solutions Ltd
  (999954, 10, 'TSL12345',
   'MJ', TRUE, NULL,
   9005, FALSE, FALSE, TRUE,
   '654325', 'TSL12345', 'Tech Solutions Ltd',
   'TECHSOLTD123', '1', '2025-08-19 09:00:00'),
   
  -- Company creditor account for Technology Partners
  (999955, 10, 'TP123456',
   'MJ', TRUE, NULL,
   9006, FALSE, FALSE, TRUE,
   '654326', 'TP123456', 'Technology Partner',
   'TECHPARTNERS123', '1', '2025-08-19 09:00:00');

INSERT INTO public.creditor_transactions (
  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
  transaction_type, transaction_amount, imposition_result_id, payment_processed,
  payment_reference, status, status_date, associated_record_type, associated_record_id
)
VALUES
  (90001, 104, '2025-08-16', 102, 'M. Davies',
   'PAYMNT', 150.00, NULL, FALSE,
   'BACS', '1', '2025-08-17', 'OTHER', NULL);
