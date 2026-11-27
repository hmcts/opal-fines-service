INSERT INTO suspense_items (
    suspense_item_id, suspense_account_id, suspense_item_number, suspense_item_type, created_date, version_number
) VALUES
    (99000000979800, 99000000979601, 100003, 'UN', '2026-11-03', 1),
    (99000000979801, 99000000979601, 100004, 'MS', '2026-11-03', 1);

INSERT INTO parties (party_id, organisation, title, forenames, surname, organisation_name,
                     address_line_1, address_line_4, address_line_5, postcode) VALUES
    (99000000979801, FALSE, 'Ms', 'Alex', 'Defendant', NULL, '1 Defendant Road', NULL, NULL, 'AB1 2CD'),
    (99000000979802, TRUE, NULL, NULL, NULL, 'Defendant Ltd', '2 Defendant Road', NULL, NULL, 'AB1 2CD'),
    (99000000979803, FALSE, 'Mr', 'Sam', 'Creditor', NULL, '3 Creditor Road', 'County', 'District', 'CD3 4EF'),
    (99000000979804, TRUE, NULL, NULL, NULL, 'Creditor Ltd', '4 Creditor Road', NULL, NULL, 'CD3 4EF'),
    (99000000979805, TRUE, NULL, NULL, NULL, 'Current Payer Ltd', '5 Payer Road', NULL, NULL, 'EF5 6GH');

INSERT INTO defendant_accounts (
    defendant_account_id, business_unit_id, account_number, amount_imposed, amount_paid, account_balance,
    account_status, account_type, version_number
) VALUES
    (99000000979801, 29101, '12345678', 100, 0, 100, 'L', 'Fine', 1),
    (99000000979802, 29101, '23456789', 100, 0, 100, 'L', 'Fine', 1),
    (99000000979803, 29102, '34567890', 100, 0, 100, 'L', 'Fine', 1),
    (99000000979804, 29101, 'ALPHANUMERIC', 100, 0, 100, 'L', 'Fine', 1),
    (99000000979805, 29101, '45678901', 100, 0, 100, 'L', 'Fine', 1),
    (99000000979806, 29101, '9223372036854775808', 100, 0, 100, 'L', 'Fine', 1);

-- Two Defendant associations are deliberately ambiguous; a Parent/Guardian must not count as a Defendant.
INSERT INTO defendant_account_parties (
    defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
) VALUES
    (99000000979801, 99000000979801, 99000000979801, 'Defendant', TRUE),
    (99000000979802, 99000000979802, 99000000979802, 'Defendant', TRUE),
    (99000000979803, 99000000979803, 99000000979801, 'Defendant', TRUE),
    (99000000979804, 99000000979804, 99000000979801, 'Defendant', TRUE),
    (99000000979805, 99000000979805, 99000000979801, 'Defendant', TRUE),
    (99000000979806, 99000000979805, 99000000979802, 'Defendant', FALSE),
    (99000000979807, 99000000979806, 99000000979801, 'Defendant', TRUE),
    (99000000979808, 99000000979801, 99000000979802, 'Parent/Guardian', FALSE);

INSERT INTO major_creditors (major_creditor_id, business_unit_id, major_creditor_code, name,
                            address_line_1, postcode) VALUES
    (99000000979801, 29101, 'SHIS', 'History Major Creditor', '6 Major Road', 'GH7 8IJ');

INSERT INTO creditor_accounts (
    creditor_account_id, business_unit_id, account_number, creditor_account_type, prosecution_service,
    major_creditor_id, minor_creditor_party_id, repayment, hold_payout, pay_by_bacs, version_number
) VALUES
    (99000000979801, 29101, 'MINOR-IND', 'MN', FALSE, NULL, 99000000979803, FALSE, FALSE, FALSE, 1),
    (99000000979802, 29101, 'MAJOR-ORG', 'MJ', FALSE, 99000000979801, NULL, FALSE, FALSE, FALSE, 1),
    (99000000979803, 29101, 'MINOR-ORG', 'MN', FALSE, NULL, 99000000979804, FALSE, FALSE, FALSE, 1),
    (99000000979804, 29101, 'REPAY-BACS', 'MN', FALSE, NULL, 99000000979803, TRUE, FALSE, TRUE, 1),
    (99000000979805, 29101, 'REPAY-CHEQUE', 'MN', FALSE, NULL, 99000000979804, TRUE, FALSE, FALSE, 1),
    (99000000979806, 29102, 'OTHER-UNIT', 'MN', FALSE, NULL, 99000000979803, TRUE, FALSE, TRUE, 1),
    (99000000979807, 29101, 'MISSING-PARTY', 'MN', FALSE, NULL, NULL, FALSE, FALSE, FALSE, 1),
    (99000000979808, 29101, 'CENTRAL-FUND', 'CF', FALSE, NULL, NULL, FALSE, FALSE, FALSE, 1);

INSERT INTO configuration_items (configuration_item_id, item_name, business_unit_id, item_values) VALUES
    (99000000979801, 'CENTRAL_FUND_ACCOUNT', 29101,
     '{"name":"History Central Fund","address_line_1":"7 Central Road","postcode":"IJ9 0KL"}');

INSERT INTO defendant_transactions (defendant_transaction_id, defendant_account_id, posted_date,
                                   transaction_type, transaction_amount) VALUES
    (99000000979801, 99000000979801, '2026-11-03', 'REPSUS', 10),
    (99000000979803, 99000000979803, '2026-11-03', 'REPSUS', 10);
INSERT INTO creditor_transactions (creditor_transaction_id, creditor_account_id, posted_date,
                                  transaction_type, transaction_amount) VALUES
    (99000000979801, 99000000979802, '2026-11-03', 'REPSUS', 10),
    (99000000979803, 99000000979806, '2026-11-03', 'REPSUS', 10),
    (99000000979804, 99000000979805, '2026-11-03', 'REPSUS', 10);
INSERT INTO impositions (imposition_id, defendant_account_id, posted_date, result_id,
                         imposed_amount, paid_amount, creditor_account_id) VALUES
    (99000000979801, 99000000979801, '2026-11-03', 'FO', 100, 0, 99000000979801),
    (99000000979802, 99000000979803, '2026-11-03', 'FO', 100, 0, 99000000979801),
    (99000000979803, 99000000979801, '2026-11-03', 'FO', 100, 0, 99000000979806);

INSERT INTO miscellaneous_accounts (miscellaneous_account_id, business_unit_id, account_number, party_id) VALUES
    (99000000979801, 29101, 'PAYER-CURRENT', 99000000979805),
    (99000000979802, 29102, 'PAYER-OTHER-UNIT', 99000000979805);
INSERT INTO tills (till_id, business_unit_id, till_number, owned_by, source) VALUES
    (99000000979801, 29101, 1, 'HISTORY', NULL),
    (99000000979802, 29101, 2, 'HISTORY', 'NATWEST');
INSERT INTO payments_in (
    payment_in_id, till_id, payment_amount, payment_date, payment_method, destination_type,
    receipt, allocated, auto_payment, associated_record_type, associated_record_id, additional_information
) VALUES
    (99000000979801, 99000000979801, 10, '2026-11-03', 'CQ', 'S', FALSE, TRUE, FALSE, NULL, NULL,
     '{"payment_received_from":"T","additional_information":{"payment_reference":"PAYER-001","payer_type":"Individual","payer_details":{"individual":{"title":"Dr","forenames":"Jo","surname":"Payer"},"address_line_1":"8 Payer Road","postcode":"KL1 2MN"}}}'),
    (99000000979802, 99000000979801, 10, '2026-11-03', 'NC', 'S', FALSE, TRUE, FALSE, NULL, NULL,
     '{"payment_received_from":"A","payer_type":"Organisation","payer_details":{"organisation":{"organisation_name":"Payer Ltd"},"address_line_1":"9 Payer Road"}}'),
    (99000000979803, 99000000979801, 10, '2026-11-03', 'PO', 'C', FALSE, TRUE, FALSE, NULL, NULL,
     '{"payment_received_from":"A","additional_information":{"payer_type":"Organisation","court_fee_code":"STALE-CODE","payer_details":{"organisation":{"organisation_name":"Court Payer Ltd"}}}}'),
    (99000000979805, 99000000979801, 10, '2026-11-03', 'NC', 'S', FALSE, TRUE, FALSE, NULL, NULL, NULL),
    (99000000979806, 99000000979802, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE, 'defendant_accounts', '99000000979801',
     '{"additional_information":{"originator_reference":"AUTO-REF","transfer_source":"OTHER"}}'),
    (99000000979807, 99000000979801, 10, '2026-11-03', 'NC', 'S', FALSE, TRUE, FALSE, 'miscellaneous_accounts', '99000000979801',
     '{"payer_type":"Organisation","payer_details":{"organisation":{"organisation_name":"Stale Payer Ltd"}}}'),
    (99000000979808, 99000000979801, 10, '2026-11-03', 'NC', 'S', FALSE, TRUE, FALSE, 'miscellaneous_accounts', '99000000979802',
     '{"payer_type":"Organisation","payer_details":{"organisation":{"organisation_name":"Must not fall back"}}}'),
    (99000000979809, 99000000979801, 10, '2026-11-03', 'NC', 'S', FALSE, TRUE, NULL, NULL, NULL, NULL),
    (99000000979810, 99000000979801, 10, '2026-11-03', 'NC', 'S', FALSE, TRUE, FALSE, NULL, NULL,
     '{"payment_received_from":"X","payer_type":"Organisation","payer_details":{"organisation":{"organisation_name":"Invalid received-from code"}}}');

INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id, text
) VALUES
    (99000000979801, 99000000979800, '2026-11-03', 'OF', 10, NULL, '99000000979801', NULL),
    (99000000979802, 99000000979800, '2026-11-03', 'FA', 10, NULL, '99000000979802', NULL),
    (99000000979803, 99000000979800, '2026-11-03', 'CF', 10, NULL, '99000000979803', NULL),
    (99000000979804, 99000000979800, '2026-11-03', 'OC', 10, NULL, '99000000979803', NULL),
    (99000000979805, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '99000000979801', NULL),
    (99000000979806, 99000000979800, '2026-11-03', 'XP', 10, 'defendant_transactions', '99000000979801', NULL),
    (99000000979807, 99000000979800, '2026-11-03', 'FA', 10, 'defendant_accounts', '99000000979802', NULL),
    (99000000979808, 99000000979800, '2026-11-03', 'CC', 10, 'creditor_accounts', '99000000979801', NULL),
    (99000000979809, 99000000979800, '2026-11-03', 'FB', 10, 'creditor_transactions', '99000000979801', NULL),
    (99000000979810, 99000000979800, '2026-11-03', 'CC', 10, 'creditor_accounts', '99000000979803', NULL),
    (99000000979811, 99000000979800, '2026-11-03', 'RP', 10, 'creditor_accounts', '99000000979804', NULL),
    (99000000979812, 99000000979800, '2026-11-03', 'RP', 10, 'creditor_transactions', '99000000979804', NULL),
    (99000000979813, 99000000979800, '2026-11-03', 'IN', 10, 'impositions', '99000000979801', NULL),
    (99000000979814, 99000000979800, '2026-11-03', 'RV', -10, NULL, NULL, 'Duplicate allocation reversed'),
    (99000000979815, 99000000979800, '2026-11-03', 'RP', 10, 'creditor_accounts', '99000000979801', NULL),
    (99000000979816, 99000000979800, '2026-11-03', 'IN', 10, 'creditor_accounts', '99000000979801', NULL),
    (99000000979817, 99000000979800, '2026-11-03', 'RV', -10, 'creditor_accounts', '99000000979801', '  '),
    (99000000979818, 99000000979800, '2026-11-03', 'XF', 10, 'suspense_items', '99000000979800', NULL),
    (99000000979819, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '99000000979999', NULL),
    (99000000979820, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '99000000979803', NULL),
    (99000000979821, 99000000979800, '2026-11-03', 'CC', 10, 'creditor_transactions', '99000000979803', NULL),
    (99000000979822, 99000000979800, '2026-11-03', 'IN', 10, 'impositions', '99000000979802', NULL),
    (99000000979823, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '99000000979804', NULL),
    (99000000979824, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '99000000979805', NULL),
    (99000000979825, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '99000000979806', NULL),
    (99000000979826, 99000000979800, '2026-11-03', 'RP', 10, 'defendant_accounts', '99000000979801', NULL),
    (99000000979827, 99000000979800, '2026-11-03', 'CF', 10, NULL, '99000000979803', NULL),
    (99000000979828, 99000000979800, '2026-11-03', 'OF', 10, NULL, '99000000979805', NULL),
    (99000000979829, 99000000979800, '2026-11-03', 'CC', 10, 'creditor_accounts', '99000000979807', NULL),
    (99000000979830, 99000000979800, '2026-11-03', 'UN', 10, NULL, '99000000979806', NULL),
    (99000000979831, 99000000979800, '2026-11-03', 'CF', 10, NULL, '99000000979803', NULL),
    (99000000979832, 99000000979800, '2026-11-03', 'DQ', 10, NULL, NULL, NULL),
    (99000000979833, 99000000979800, '2026-11-03', 'IN', 10, 'impositions', '99000000979803', NULL),
    (99000000979834, 99000000979800, '2026-11-03', 'CC', 10, 'creditor_accounts', '99000000979808', NULL),
    (99000000979835, 99000000979800, '2026-11-03', 'OF', 10, NULL, '99000000979807', NULL),
    (99000000979836, 99000000979800, '2026-11-03', 'OF', 10, NULL, '99000000979808', NULL),
    (99000000979837, 99000000979800, '2026-11-03', 'CF', 10, NULL, '99000000979803', NULL),
    (99000000979838, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_transactions', '99000000979803', NULL),
    (99000000979839, 99000000979800, '2026-11-03', 'OF', 10, 'impositions', '99000000979801', NULL),
    (99000000979840, 99000000979801, '2026-11-03', 'UN', 10, NULL, '99000000979801', NULL),
    (99000000979841, 99000000979800, '2026-11-03', 'OF', 10, NULL, '99000000979809', NULL),
    (99000000979842, 99000000979800, '2026-11-03', 'OF', 10, NULL, '99000000979810', NULL);

INSERT INTO court_fees (court_fee_id, business_unit_id, court_fee_code, description, amount, stats_code) VALUES
    (99000000979801, 29101, 'HISTORY', 'History court fee', 10, 'HISTORY'),
    (99000000979802, 29102, 'OTHER', 'Other unit court fee', 10, 'HISTORY');
INSERT INTO court_fees_received (court_fee_received_id, business_unit_id, court_fee_id, overpayment,
                                 suspense_transaction_id, number_of_items) VALUES
    (99000000979801, 29101, 99000000979801, FALSE, 99000000979803, 1),
    (99000000979802, 29101, 99000000979801, TRUE, 99000000979804, 1),
    (99000000979803, 29102, 99000000979802, FALSE, 99000000979831, 1),
    (99000000979804, 29101, 99000000979801, FALSE, 99000000979837, 1),
    (99000000979805, 29101, 99000000979801, FALSE, 99000000979837, 1);

-- A resolved creditor does not make an imposition a supported RP reference.
INSERT INTO impositions (imposition_id, defendant_account_id, posted_date, result_id,
                         imposed_amount, paid_amount, creditor_account_id) VALUES
    (99000000979804, 99000000979801, '2026-11-03', 'FO', 100, 0, 99000000979804);

-- References must preserve bigint lookups without accepting overflow or noncanonical text.
INSERT INTO defendant_accounts (
    defendant_account_id, business_unit_id, account_number, amount_imposed, amount_paid, account_balance,
    account_status, account_type, version_number
) VALUES (9000000000000000001, 29101, '87654321', 100, 0, 100, 'L', 'Fine', 1);
INSERT INTO defendant_account_parties (
    defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
) VALUES (99000000979809, 9000000000000000001, 99000000979801, 'Defendant', TRUE);
INSERT INTO payments_in (
    payment_in_id, till_id, payment_amount, payment_date, payment_method, destination_type,
    receipt, allocated, auto_payment, associated_record_type, associated_record_id, additional_information
) VALUES
    (99000000979811, 99000000979801, 10, '2026-11-03', 'NC', 'S', FALSE, TRUE, FALSE,
     'miscellaneous_accounts', '9223372036854775808',
     '{"payer_type":"Organisation","payer_details":{"organisation":{"organisation_name":"No fallback"}}}'),
    (99000000979812, 99000000979802, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE,
     'defendant_accounts', '9223372036854775808', '{"originator_reference":"OVERFLOW-ACCOUNT"}');
INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id
) VALUES
    (99000000979843, 99000000979800, '2026-11-03', 'RP', 10, 'impositions', '99000000979804'),
    (99000000979844, 99000000979800, '2026-11-03', 'UN', 10, NULL, '9223372036854775808'),
    (99000000979845, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '-9223372036854775809'),
    (99000000979846, 99000000979800, '2026-11-03', 'OF', 10, NULL, '99000000979811'),
    (99000000979847, 99000000979800, '2026-11-03', 'UN', 10, NULL, '099000000979806'),
    (99000000979848, 99000000979800, '2026-11-03', 'CC', 10, 'creditor_accounts', '099000000979801'),
    (99000000979849, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_accounts', '9000000000000000001'),
    (99000000979850, 99000000979800, '2026-11-03', 'UN', 10, NULL, '99000000979812'),
    (99000000979851, 99000000979800, '2026-11-03', 'OF', 10, 'defendant_transactions', '99000000979801'),
    (99000000979852, 99000000979800, '2026-11-03', 'RP', 10, 'defendant_accounts', '99000000979802'),
    (99000000979853, 99000000979800, '2026-11-03', 'RP', 10, 'defendant_accounts', '99000000979803'),
    (99000000979854, 99000000979800, '2026-11-03', 'RP', 10, 'defendant_accounts', '99000000979999'),
    (99000000979855, 99000000979800, '2026-11-03', 'RP', 10, 'defendant_accounts', '99000000979804'),
    (99000000979856, 99000000979800, '2026-11-03', 'RP', 10, 'defendant_accounts', '99000000979805'),
    (99000000979857, 99000000979800, '2026-11-03', 'RP', 10, 'defendant_transactions', '99000000979801'),
    (99000000979858, 99000000979801, '2026-11-03', 'MS', 10, NULL, '99000000979801');

INSERT INTO suspense_items (
    suspense_item_id, suspense_account_id, suspense_item_number, suspense_item_type, created_date, version_number
) VALUES (99000000979802, 99000000979601, 100005, 'OF', '2026-11-03', 1);
INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id, text
) VALUES
    (99000000979859, 99000000979800, '2026-11-03', 'DQ', -10, NULL, NULL, 'Dishonoured payment'),
    (99000000979860, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979805', NULL);

-- OC fallback requires exactly one received row in this business unit.
INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id
) VALUES
    (99000000979861, 99000000979800, '2026-11-03', 'OC', 10, NULL, '99000000979803'),
    (99000000979862, 99000000979800, '2026-11-03', 'OC', 10, NULL, '99000000979803'),
    (99000000979863, 99000000979800, '2026-11-03', 'OC', 10, NULL, '99000000979803'),
    (99000000979864, 99000000979800, '2026-11-03', 'OC', 10, NULL, '99000000979803');
INSERT INTO court_fees_received (
    court_fee_received_id, business_unit_id, court_fee_id, overpayment, suspense_transaction_id, number_of_items
) VALUES
    (99000000979806, 29101, NULL, TRUE, 99000000979861, 1),
    (99000000979807, 29101, 99000000979801, TRUE, 99000000979862, 1),
    (99000000979808, 29101, NULL, TRUE, 99000000979862, 1),
    (99000000979809, 29101, 99000000979802, TRUE, 99000000979863, 1),
    (99000000979810, 29102, NULL, TRUE, 99000000979864, 1);

-- Required detail fields: missing payer and creditor-address records must not produce invalid objects.
INSERT INTO parties (party_id, organisation, forenames, surname) VALUES
    (99000000979806, FALSE, 'No', 'Address');
INSERT INTO creditor_accounts (
    creditor_account_id, business_unit_id, account_number, creditor_account_type,
    prosecution_service, minor_creditor_party_id, repayment, hold_payout, pay_by_bacs, version_number
) VALUES (99000000979809, 29101, 'NO-ADDRESS', 'MN', FALSE, 99000000979806, TRUE, FALSE, TRUE, 1);
INSERT INTO impositions (imposition_id, defendant_account_id, posted_date, result_id,
                         imposed_amount, paid_amount, creditor_account_id) VALUES
    (99000000979805, 99000000979801, '2026-11-03', 'FO', 100, 0, 99000000979809);
INSERT INTO payments_in (
    payment_in_id, till_id, payment_amount, payment_date, payment_method, destination_type,
    receipt, allocated, auto_payment, additional_information
) VALUES (99000000979813, 99000000979801, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, FALSE,
    '{"transfer_source":"VARIANT_BANKING"}');
INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id
) VALUES
    (99000000979865, 99000000979800, '2026-11-03', 'CF', 10, NULL, '99000000979805'),
    (99000000979866, 99000000979800, '2026-11-03', 'CC', 10, 'creditor_accounts', '99000000979809'),
    (99000000979867, 99000000979800, '2026-11-03', 'RP', 10, 'creditor_accounts', '99000000979809'),
    (99000000979868, 99000000979800, '2026-11-03', 'IN', 10, 'impositions', '99000000979805'),
    (99000000979869, 99000000979800, '2026-11-03', 'UN', 10, NULL, '99000000979813');
INSERT INTO court_fees_received (
    court_fee_received_id, business_unit_id, court_fee_id, overpayment, suspense_transaction_id, number_of_items
) VALUES (99000000979811, 29101, 99000000979801, FALSE, 99000000979865, 1);

-- A nonempty UN originator reference selects automatic details; OF also checks the payment association.
INSERT INTO payments_in (
    payment_in_id, till_id, payment_amount, payment_date, payment_method, destination_type,
    receipt, allocated, auto_payment, associated_record_type, associated_record_id, additional_information
) VALUES
    (99000000979814, 99000000979801, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, FALSE, NULL, NULL,
     '{"originator_reference":"MANUAL-REF","transfer_source":"VARIANT_BANKING","suspense_reason_detail":"Unknown payer"}'),
    (99000000979815, 99000000979802, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE, NULL, NULL,
     '{"originator_reference":""}'),
    (99000000979816, 99000000979802, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE, NULL, NULL,
     '{"originator_reference":" "}'),
    (99000000979817, 99000000979802, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE,
     'defendant_accounts', '99000000979804', '{"originator_reference":"NONNUMERIC-ACCOUNT"}'),
    (99000000979818, 99000000979802, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, TRUE,
     'defendant_accounts', '99000000979806', '{"originator_reference":"OVERFLOW-ACCOUNT-NUMBER"}');
INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id
) VALUES
    (99000000979870, 99000000979800, '2026-11-03', 'UN', 10, NULL, '99000000979814'),
    (99000000979871, 99000000979800, '2026-11-03', 'UN', 10, NULL, '99000000979801'),
    (99000000979872, 99000000979800, '2026-11-03', 'UN', 10, NULL, '99000000979815'),
    (99000000979873, 99000000979800, '2026-11-03', 'UN', 10, NULL, '99000000979816'),
    (99000000979874, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979806'),
    (99000000979875, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979812'),
    (99000000979876, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979814'),
    (99000000979877, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979817'),
    (99000000979878, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979818'),
    (99000000979879, 99000000979802, '2026-11-03', 'UN', 10, NULL, '99000000979806'),
    (99000000979880, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979813');

-- Contrast miscellaneous-account payments with defendant-account payments on the same OF item.
INSERT INTO payments_in (
    payment_in_id, till_id, payment_amount, payment_date, payment_method, destination_type,
    receipt, allocated, auto_payment, associated_record_type, associated_record_id, additional_information
) VALUES (99000000979819, 99000000979802, 10, '2026-11-03', 'CT', 'S', FALSE, TRUE, FALSE,
    'defendant_accounts', '99000000979801', '{"originator_reference":"DEFENDANT-MANUAL"}');
INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount,
    associated_record_type, associated_record_id
) VALUES
    (99000000979881, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979807'),
    (99000000979882, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979808'),
    (99000000979883, 99000000979802, '2026-11-03', 'OF', 10, NULL, '99000000979819');
