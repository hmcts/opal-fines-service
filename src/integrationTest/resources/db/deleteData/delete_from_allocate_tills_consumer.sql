DELETE FROM report_instances WHERE business_unit_id = ARRAY[9432]::smallint[];
DELETE FROM suspense_transactions
WHERE suspense_item_id IN (
    SELECT suspense_item_id FROM suspense_items WHERE suspense_account_id = 9999003432001
);
DELETE FROM suspense_items WHERE suspense_account_id = 9999003432001;
DELETE FROM payments_in WHERE payment_in_id = 9999003432001;
DELETE FROM suspense_item_number_index WHERE business_unit_id = 9432;
DELETE FROM suspense_accounts WHERE suspense_account_id = 9999003432001;
DELETE FROM tills WHERE till_id = 9999003432001;
DELETE FROM business_units WHERE business_unit_id = 9432;
