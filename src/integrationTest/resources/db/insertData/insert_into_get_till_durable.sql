INSERT INTO parties (party_id, organisation, forenames, surname, organisation_name) VALUES
(99000000836270, false, 'Durable Sam', 'Durable Smith', NULL),
(99000000836271, true, NULL, NULL, 'Durable Organisation');

INSERT INTO miscellaneous_accounts (miscellaneous_account_id, business_unit_id, account_number, party_id) VALUES
(99000000836280, 8362, 'GTIL-MISC-1', 99000000836270),
(99000000836281, 8362, 'GTIL-MISC-2', 99000000836271);

INSERT INTO payments_in (payment_in_id, till_id, payment_amount, payment_date, payment_method,
    destination_type, associated_record_type, associated_record_id, third_party_payer_name,
    additional_information, receipt, allocated, auto_payment) VALUES
(99000000836290, 99000000836200, 10.00, '2026-09-09 12:00:00', 'CT', 'S',
    'miscellaneous_accounts', '99000000836280', 'First Class Third Party', '{"payment_received_from":"T"}',
    false, false, false),
(99000000836291, 99000000836200, 20.00, '2026-09-09 12:01:00', 'CT', 'C',
    'miscellaneous_accounts', '99000000836281', NULL, '{"payment_received_from":"T"}', false, false, false),
(99000000836292, 99000000836200, 5.00, '2026-09-09 12:02:00', 'CT', 'S',
    'miscellaneous_accounts', '99000000836281', NULL, '{"payment_received_from":"T"}', false, false, false);
