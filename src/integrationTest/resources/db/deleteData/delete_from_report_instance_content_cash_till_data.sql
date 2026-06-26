DELETE FROM payments_in
WHERE payment_in_id = 99000000353300;

DELETE FROM report_instances
WHERE report_instance_id = 99000000353000;

DELETE FROM defendant_accounts
WHERE defendant_account_id = 99000000353200;

DELETE FROM tills
WHERE till_id = 99000000353100;

DELETE FROM business_units
WHERE business_unit_id = 1778;

UPDATE reports
SET retention_period = '14',
    permission = NULL
WHERE report_id = 'cash_till';
