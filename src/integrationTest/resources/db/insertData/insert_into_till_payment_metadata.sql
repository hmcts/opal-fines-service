-- PO-11365: database-only fixtures, independent of the Add Till API implementation.
-- Each test runs in a rollback transaction, including this fixture.
INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
VALUES (9365, 'PO-11365 DB tests', 'DB65', 'Accounting Division');

INSERT INTO parties (party_id, organisation, surname)
VALUES (113651000, FALSE, 'Durable payer');

INSERT INTO miscellaneous_accounts (miscellaneous_account_id, business_unit_id, account_number, party_id)
VALUES (113651000, 9365, 'DB11365-MISC', 113651000);

INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number, amount_imposed,
    amount_paid, account_balance, account_status, account_type, version_number)
VALUES (113651000, 9365, 'DB11365-DEF', 0, 0, 0, 'L', 'Fine', 0);

INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number)
VALUES (113651000, 9365, 'DB11365-SUSPENSE');

INSERT INTO court_fees (court_fee_id, business_unit_id, court_fee_code, description, amount, stats_code)
VALUES (113651000, 9365, 'DBFEE', 'PO-11365 test fee', 5.00, 'DB65');

INSERT INTO tills (till_id, business_unit_id, till_number, owned_by, owned_by_name, status,
    created_date, total_amount, payments_count, auto_payment)
SELECT fixture.till_id, 9365, fixture.till_number, 'DB11365', 'DB test', 'Created',
       CURRENT_TIMESTAMP, 12.50, fixture.payments_count, fixture.auto_payment
  FROM (VALUES
      (113651001, 1, 1, FALSE), (113651002, 2, 1, FALSE),
      (113651003, 3, 1, FALSE), (113651004, 4, 1, FALSE),
      (113651005, 5, 2, FALSE), (113651006, 6, 1, FALSE),
      (113651007, 7, 1, FALSE), (113651008, 8, 1, TRUE)
  ) AS fixture(till_id, till_number, payments_count, auto_payment);

INSERT INTO payments_in (payment_in_id, till_id, payment_amount, payment_date, payment_method,
    destination_type, allocation_type, associated_record_type, associated_record_id,
    third_party_payer_name, additional_information, receipt, allocated, auto_payment)
SELECT fixture.payment_in_id, fixture.till_id, fixture.amount, CURRENT_TIMESTAMP, 'NC',
       fixture.destination::t_pi_destination_type_enum, fixture.allocation_type,
       fixture.associated_type::t_associated_record_type_enum,
       CASE WHEN fixture.associated_type IS NOT NULL THEN '113651000' END,
       fixture.third_party_name,
       (CASE WHEN fixture.auto_payment THEN '{}'::JSONB ELSE
           '{"payment_received_from":"D","payment_reference":"DB-REFERENCE","transfer_source":"NATWEST"}'::JSONB
        END || fixture.information::JSONB)::JSON,
       FALSE, FALSE, fixture.auto_payment
  FROM (VALUES
      (113651001, 113651001, 12.50, 'S', 'OF', 'miscellaneous_accounts', NULL, FALSE,
       '{"suspense_reason_code":"OF","suspense_note":"DB payment note", "payer_type":"Individual",
         "payer_details":{"individual":{"surname":"Transient payer"},"address_line_1":"Transient address"}}'),
      (113651002, 113651002, 12.50, 'S', 'FA', 'miscellaneous_accounts', NULL, FALSE,
       '{"suspense_reason_code":"FA","suspense_note":"DB payment note"}'),
      (113651003, 113651003, 12.50, 'S', 'MS', 'miscellaneous_accounts', NULL, FALSE,
       '{"suspense_reason_code":"MS","suspense_note":"DB payment note"}'),
      (113651004, 113651004, 12.50, 'S', 'UN', NULL, NULL, FALSE,
       '{"suspense_reason_code":"UN","suspense_reason_detail":"Unidentified receipt", "suspense_note":"DB payment note",
         "payment_reference":"","transfer_source":null}'),
      (113651005, 113651005, 10.00, 'C', 'FULL', 'miscellaneous_accounts', NULL, FALSE,
       '{"payment_received_from":"A","court_fee_code":"DBFEE","number_of_items":2,
         "suspense_note":"DB payment note","court_fee_overpayment":false}'),
      (113651006, 113651005, 2.50, 'C', 'FULL', 'miscellaneous_accounts', NULL, FALSE,
       '{"payment_received_from":"A","court_fee_code":"DBFEE","number_of_items":2,
         "suspense_note":"DB payment note","court_fee_overpayment":true}'),
      (113651007, 113651006, 12.50, 'C', 'FULL', 'miscellaneous_accounts', NULL, FALSE,
       '{"additional_information":{"payment_received_from":"A","payment_reference":"DB-REFERENCE",
         "transfer_source":"NATWEST","court_fee_code":"DBFEE","number_of_items":2,"suspense_note":"DB payment note"}}'),
      (113651008, 113651007, 12.50, 'F', 'FULL', 'defendant_accounts', 'Criminal third party', FALSE, '{}'),
      (113651009, 113651008, 12.50, 'S', 'UN', 'defendant_accounts', NULL, TRUE,
       '{"originator_reference":"AUTO-REFERENCE","payment_received_from":"T","source":"ALLPAY"}')
  ) AS fixture(payment_in_id, till_id, amount, destination, allocation_type,
               associated_type, third_party_name, auto_payment, information);
