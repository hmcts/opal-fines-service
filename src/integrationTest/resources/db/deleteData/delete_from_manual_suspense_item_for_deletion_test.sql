DELETE FROM notes
WHERE note_id = 99000000836540;

DELETE FROM court_fees_received
WHERE court_fee_received_id IN (99000000836530, 99000000836531);

DELETE FROM control_totals
WHERE control_total_id IN (99000000836520, 99000000836521);

DELETE FROM suspense_transactions
WHERE suspense_transaction_id IN (99000000836510, 99000000836511);

DELETE FROM suspense_items
WHERE suspense_item_id = 99000000836503;

DELETE FROM suspense_accounts
WHERE suspense_account_id = 99000000836502;

DELETE FROM court_fees
WHERE court_fee_id IN (99000000836501, 99000000836502);

DELETE FROM business_units
WHERE business_unit_id = 8365;
