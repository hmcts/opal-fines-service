INSERT INTO business_units (
    business_unit_id, business_unit_name, business_unit_code, business_unit_type, opal_domain
) VALUES
    (29101, 'Suspense Summary A', 'SSA', 'Area', 'Fines'),
    (29102, 'Suspense Summary B', 'SSB', 'Area', 'Fines');

INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number) VALUES
    (99000000979601, 29101, 'SUMMARY-A'),
    (99000000979602, 29102, 'SUMMARY-B');

-- Reuse an item number across units to prove that both lookup predicates are applied.
INSERT INTO suspense_items (
    suspense_item_id, suspense_account_id, suspense_item_number, suspense_item_type, created_date
) VALUES
    (99000000979601, 99000000979601, 100000, 'CF', '2026-11-03 10:15:30'),
    (99000000979602, 99000000979602, 100000, 'UN', '2026-11-04 11:20:00'),
    (99000000979603, 99000000979601, 3000000000, 'UN', '2026-11-05 12:25:00');

-- The third item deliberately has no index entry, as may occur for legacy items.
INSERT INTO suspense_item_number_index (
    suspense_item_number_index_id, business_unit_id, suspense_item_number
) VALUES
    (99000000979601, 29101, 100000),
    (99000000979602, 29102, 100000);

INSERT INTO suspense_transactions (
    suspense_transaction_id, suspense_item_id, posted_date, transaction_type, amount
) VALUES
    (99000000979601, 99000000979601, '2026-11-03 10:15:30', 'CF', 100.50),
    (99000000979602, 99000000979601, '2026-11-03 11:15:30', 'XF', -25.25);
