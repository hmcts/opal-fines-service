DELETE FROM configuration_items WHERE configuration_item_id = 978020;
DELETE FROM creditor_accounts WHERE creditor_account_id IN (978010, 978011);
DELETE FROM major_creditors WHERE major_creditor_id = 978001;
DELETE FROM business_units WHERE business_unit_id = 978;
