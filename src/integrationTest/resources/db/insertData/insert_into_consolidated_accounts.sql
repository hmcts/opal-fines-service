INSERT INTO defendant_accounts (
    defendant_account_id, version_number, business_unit_id, account_number, imposed_hearing_date,
    amount_paid, account_balance, amount_imposed, account_status, allow_writeoffs, allow_cheques,
    account_type, collection_order, payment_card_requested, originator_name, imposed_by_name,
    prosecutor_case_reference
) VALUES
    (233300, 12, 78, '233300M', TIMESTAMP '2026-01-21 10:15:00',
     0.00, 100.00, 100.00, 'L', 'N', 'N', 'Fine', 'N', 'N', 'PO-2333 Court', 'Master Court', 'MASTER-REF'),
    (233301, 3, 78, '233301C', TIMESTAMP '2026-01-21 10:15:00',
     0.00, 100.00, 100.00, 'L', 'N', 'N', 'Fine', 'N', 'N', 'PO-2333 Court', 'Child Court', 'CHILD-REF'),
    (233302, 4, 78, '233302M', TIMESTAMP '2026-01-21 10:15:00',
     0.00, 100.00, 100.00, 'L', 'N', 'N', 'Fine', 'N', 'N', 'PO-2333 Court', 'Other Master Court', 'OTHER-MASTER'),
    (233303, 5, 78, '233303C', TIMESTAMP '2026-01-21 10:15:00',
     0.00, 100.00, 100.00, 'L', 'N', 'N', 'Fine', 'N', 'N', 'PO-2333 Court', 'Other Child Court', 'OTHER-REF'),
    (233304, 6, 78, '233304M', TIMESTAMP '2026-01-21 10:15:00',
     0.00, 100.00, 100.00, 'L', 'N', 'N', 'Fine', 'N', 'N', 'PO-2333 Court', 'Empty Master Court', 'EMPTY-REF');

INSERT INTO parties (party_id, organisation, forenames, surname)
VALUES
    (233301, FALSE, 'Alex', 'Jones'),
    (233303, FALSE, 'Casey', 'Smith');

INSERT INTO defendant_account_parties (
    defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
) VALUES
    (233301, 233301, 233301, 'Defendant', TRUE),
    (233303, 233303, 233303, 'Defendant', TRUE);

INSERT INTO defendant_transactions (
    defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type,
    transaction_amount, status_date, associated_record_type, associated_record_id, status, posted_by_name
) VALUES
    (23330001, 233300, TIMESTAMP '2026-01-21 12:00:00', 'po2333', 'CONSOL', 0.00,
     TIMESTAMP '2026-01-21 12:00:00', 'defendant_accounts', '233301', 'P', 'PO-2333 User'),
    (23330002, 233302, TIMESTAMP '2026-01-21 12:00:00', 'po2333', 'CONSOL', 0.00,
     TIMESTAMP '2026-01-21 12:00:00', 'defendant_accounts', '233303', 'P', 'PO-2333 User');
