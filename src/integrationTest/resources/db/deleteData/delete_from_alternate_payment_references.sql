DELETE
FROM alternate_payment_references
WHERE alternate_payment_reference_id IN (10877001, 10877002, 10877003);
DELETE
FROM defendant_accounts
WHERE defendant_account_id IN (1087701, 1087702, 1087703);
DELETE
FROM business_units
WHERE business_unit_id = 10877;
