DELETE FROM payments_in
WHERE payment_in_id IN (99000000343700, 99000000343800);

DELETE FROM report_instances
WHERE report_instance_id = 99000000343000;

DELETE FROM defendant_account_parties
WHERE defendant_account_party_id = 99000000343400;

DELETE FROM parties
WHERE party_id = 99000000343300;

DELETE FROM suspense_items
WHERE suspense_item_id = 99000000343600;

DELETE FROM suspense_accounts
WHERE suspense_account_id = 99000000343500;

DELETE FROM defendant_accounts
WHERE defendant_account_id = 99000000343200;

DELETE FROM tills
WHERE till_id = 99000000343100;

DELETE FROM business_units
WHERE business_unit_id = 1777;

DELETE FROM reports
WHERE report_id = 'cash_list';
