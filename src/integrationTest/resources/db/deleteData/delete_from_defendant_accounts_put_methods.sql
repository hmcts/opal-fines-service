DELETE FROM amendments
WHERE associated_record_id IN ('22005', '22006');

-- Delete company/individual aliases (AC9d/AC9di and others)
DELETE FROM aliases
WHERE alias_id IN (2200501, 2200502)
OR party_id IN (22005, 22006);

-- Remove defendant_account_parties links
DELETE FROM defendant_account_parties
WHERE defendant_account_party_id IN (22005, 22006)
OR defendant_account_id IN (22005, 22006);

-- Remove from fixed_penalty_offences (safe even if none exist)
DELETE FROM fixed_penalty_offences
WHERE defendant_account_id IN (22005, 22006);

-- Remove payment_terms (safe even if none exist)
DELETE FROM payment_terms
WHERE defendant_account_id IN (22005, 22006);

-- Remove notes (ASSOCIATED_RECORD_ID is varchar)
DELETE FROM notes
WHERE associated_record_id IN ('22005', '22006');

-- Remove main defendant accounts
DELETE FROM defendant_accounts
WHERE defendant_account_id IN (22005, 22006);

-- Remove from debtor_detail before removing parties
DELETE FROM debtor_detail
WHERE party_id IN (22005, 22006);

-- Remove inserted parties
DELETE FROM parties
WHERE party_id IN (22005, 22006);
