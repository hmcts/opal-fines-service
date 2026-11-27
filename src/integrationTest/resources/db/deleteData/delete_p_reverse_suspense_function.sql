DELETE FROM report_entries
WHERE business_unit_id = 3977
  AND report_id = 'reversed_payments_report';

DELETE FROM suspense_transactions
WHERE suspense_item_id BETWEEN 99000001035001 AND 99000001035008;

DELETE FROM suspense_items
WHERE suspense_item_id BETWEEN 99000001035001 AND 99000001035008;

DELETE FROM suspense_accounts
WHERE suspense_account_id = 99000001035000;

DELETE FROM business_units
WHERE business_unit_id = 3977;

DELETE FROM reports
WHERE report_id = 'reversed_payments_report'
  AND report_title = 'PO-10350 Test Report Fixture';
