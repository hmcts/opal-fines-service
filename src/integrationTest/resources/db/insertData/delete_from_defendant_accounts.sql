-- Delete company aliases (AC9d/AC9di and others)
DELETE FROM aliases WHERE alias_id IN (
  8801, 9011, 5551, 5552, 6661, 6662, 7771
);

-- Also cover any aliases by party for full safety
DELETE FROM aliases WHERE party_id IN (77,88,901,333,555,666,777,444,999);

-- Remove defendant_account_parties links
DELETE FROM defendant_account_parties WHERE defendant_account_party_id IN (77,88,901,333,555,666,777,444,999);

-- Remove from fixed_penalty_offences
DELETE FROM fixed_penalty_offences WHERE defendant_account_id IN (77,88,901,333,555,666,777,444,999);

-- Remove payment_terms
DELETE FROM payment_terms WHERE defendant_account_id IN (77,88,901,333,555,666,777,444,999);

-- Remove notes (ASSOCIATED_RECORD_ID is varchar, so use quoted strings!)
DELETE FROM notes WHERE associated_record_id IN ('77','88','901','333','555','666','777','444','999');

-- Remove main defendant accounts
DELETE FROM defendant_accounts WHERE defendant_account_id IN (77,88,901,333,555,666,777,444,999);

-- Remove inserted parties
DELETE FROM parties WHERE party_id IN (77,88,901,333,555,666,777,444,999);

-- Remove from draft_accounts referencing test business units
DELETE FROM draft_accounts WHERE business_unit_id IN (78, 9999);

-- Remove from creditor_accounts referencing test business units
DELETE FROM creditor_accounts WHERE business_unit_id IN (78, 9999);

-- Now safe to remove any business units you inserted just for tests
DELETE FROM business_units WHERE business_unit_id IN (78,9999);
