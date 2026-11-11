DELETE FROM suspense_transactions WHERE suspense_transaction_id IN (99000000979601, 99000000979602);
DELETE FROM suspense_item_number_index WHERE suspense_item_number_index_id IN (99000000979601, 99000000979602);
DELETE FROM suspense_items WHERE suspense_item_id IN (99000000979601, 99000000979602, 99000000979603);
DELETE FROM suspense_accounts WHERE suspense_account_id IN (99000000979601, 99000000979602);
DELETE FROM business_units WHERE business_unit_id IN (29101, 29102);
