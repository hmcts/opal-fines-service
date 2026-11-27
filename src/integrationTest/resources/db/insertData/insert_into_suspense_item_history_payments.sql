INSERT INTO suspense_items (
    suspense_item_id, suspense_account_id, suspense_item_number, suspense_item_type, created_date, version_number
) VALUES
    (99000000979700, 99000000979601, 100001, 'UN', '2026-11-03 10:00:00', 1),
    (99000000979701, 99000000979601, 100002, 'MS', '2026-11-03 10:00:00', 1);

INSERT INTO tills (till_id, business_unit_id, till_number, owned_by, source) VALUES
    (99000000979701, 29101, 1, 'HISTORY', 'NATWEST'),
    (99000000979702, 29101, 2, 'HISTORY', NULL),
    (99000000979703, 29102, 1, 'HISTORY', 'DWP');

INSERT INTO payments_in (
    payment_in_id, till_id, payment_amount, payment_date, payment_method, destination_type,
    receipt, allocated, auto_payment, additional_information
) VALUES
    (99000000979701, 99000000979701, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE,
     '{"interface_name":"Bank import job","additional_information":{"originator_reference":"BANK-REFERENCE"}}'),
    (99000000979702, 99000000979701, 20, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE,
     '{"originator_reference":"FLAT-REFERENCE"}'),
    (99000000979703, 99000000979702, 30, '2026-11-03', 'CQ', 'S', FALSE, TRUE, FALSE,
     '{"payment_received_from":"T","additional_information":{"originator_reference":"MANUAL-CHEQUE","payment_reference":"CHEQUE-123","transfer_source":"ALLPAY","suspense_reason_detail":"Payer unknown"}}'),
    (99000000979704, 99000000979702, 40, '2026-11-03', 'NC', 'S', FALSE, TRUE, FALSE,
     '{"payment_received_from":"D","originator_reference":"MANUAL-CASH","payment_reference":"CASH-456","transfer_source":"OTHER","suspense_reason_detail":"No account reference"}'),
    (99000000979705, 99000000979702, 50, '2026-11-03', 'PO', 'S', FALSE, TRUE, FALSE, NULL),
    (99000000979708, 99000000979703, 80, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE,
     '{"originator_reference":"OTHER-UNIT-SECRET"}'),
    (99000000979713, 99000000979701, 13, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE,
     '{"interface_name":"Bank import job","additional_information":{"originator_reference":null}}'),
    (99000000979714, 99000000979702, 14, '2026-11-03', 'CT', 'S', FALSE, TRUE, FALSE,
     '{"payment_received_from":"A","additional_information":{"originator_reference":"MANUAL-APPLICANT","payment_reference":"APPLICANT-789"}}');

-- Include references which must not be interpreted as linked payments.
INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id
) VALUES
    (99000000979701, 99000000979700, '2026-11-03 11:00:00', 'UN', 10, NULL, '99000000979701'),
    (99000000979702, 99000000979700, '2026-11-03 11:00:00', 'UN', 20, NULL, '99000000979702'),
    (99000000979703, 99000000979700, '2026-11-03 11:00:00', 'UN', 30, NULL, '99000000979703'),
    (99000000979704, 99000000979700, '2026-11-03 11:00:00', 'UN', 40, NULL, '99000000979704'),
    (99000000979705, 99000000979700, '2026-11-03 11:00:00', 'UN', 50, NULL, '99000000979705'),
    (99000000979706, 99000000979700, '2026-11-03 11:00:00', 'UN', 60, NULL, 'NON-NUMERIC'),
    (99000000979707, 99000000979700, '2026-11-03 11:00:00', 'UN', 70, 'defendant_accounts', '99000000979701'),
    (99000000979708, 99000000979700, '2026-11-03 11:00:00', 'UN', 80, NULL, '99000000979708'),
    (99000000979709, 99000000979700, '2026-11-03 11:00:00', 'XF', -10, NULL, '99000000979701'),
    (99000000979710, 99000000979700, '2026-11-03 11:00:00', 'CF', 30, NULL, '99000000979703'),
    (99000000979711, 99000000979701, '2026-11-03 11:00:00', 'UN', 30, NULL, '99000000979703'),
    (99000000979712, 99000000979700, '2026-11-03 11:00:00', 'UN', 120, NULL, '99000000979999'),
    (99000000979713, 99000000979700, '2026-11-03 11:00:00', 'UN', 13, NULL, '99000000979713'),
    (99000000979714, 99000000979700, '2026-11-03 11:00:00', 'UN', 14, NULL, '99000000979714'),
    (99000000979715, 99000000979700, '2026-11-03 11:00:00', 'XP', -10, NULL, '99000000979701');
