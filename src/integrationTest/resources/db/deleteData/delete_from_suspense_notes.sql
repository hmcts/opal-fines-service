DELETE FROM notes WHERE associated_record_id IN ('99000000104031', '99000000104032');
DELETE FROM suspense_items WHERE suspense_item_id IN (99000000104031, 99000000104032);
DELETE FROM suspense_accounts WHERE suspense_account_id = 99000000104030;
DELETE FROM business_units WHERE business_unit_id = 10403;
