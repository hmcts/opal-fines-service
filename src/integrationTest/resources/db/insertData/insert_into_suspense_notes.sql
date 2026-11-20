INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type, welsh_language)
VALUES (10403, 'Suspense Notes Test', 'SN03', 'Area', false);

INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number)
VALUES (99000000104030, 10403, 'SUSPENSE-NOTES');

INSERT INTO suspense_items (suspense_item_id, suspense_account_id, suspense_item_number,
                           suspense_item_type, created_date, version_number)
VALUES (99000000104031, 99000000104030, 1, 'UN', CURRENT_TIMESTAMP, 3),
       (99000000104032, 99000000104030, 2, 'UN', CURRENT_TIMESTAMP, NULL);
