DELETE
FROM alternate_payment_references
WHERE business_unit_id = 10877;
DELETE
FROM notes
WHERE associated_record_id IN ('1087701', '1087702', '1087703');
DELETE
FROM defendant_accounts
WHERE defendant_account_id IN (1087701, 1087702, 1087703);
DELETE
FROM business_units
WHERE business_unit_id = 10877;
