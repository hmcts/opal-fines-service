-- Delete company aliases (AC9d/AC9di and others)
DELETE FROM aliases WHERE alias_id IN (8801, 9011, 5551, 5552, 6661, 6662, 7771, 10001, 10002, 10003, 10004, 10005);

-- Also cover any aliases by party for full safety
DELETE FROM aliases WHERE party_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999);

-- Remove defendant_account_parties links
DELETE FROM defendant_account_parties
WHERE defendant_account_party_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 9077);

-- Remove from fixed_penalty_offences (safe even if none exist)
DELETE FROM fixed_penalty_offences
WHERE defendant_account_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999, 9077);

-- Remove payment_terms (safe even if none exist)
DELETE FROM payment_terms
WHERE defendant_account_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 9077);

-- Remove notes (ASSOCIATED_RECORD_ID is varchar)
DELETE FROM notes
WHERE associated_record_id IN ('77', '88', '901', '333', '555', '666', '777', '444', '999', '9077');

-- Remove main defendant accounts
DELETE FROM defendant_accounts
WHERE defendant_account_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 9077);

-- Remove from debtor_detail before removing parties
DELETE FROM debtor_detail WHERE party_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004);

-- Remove inserted parties
DELETE FROM parties WHERE party_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004);

-- Remove from draft_accounts referencing test BU
DELETE FROM draft_accounts WHERE business_unit_id = 9999;

-- Remove from enforcers referencing test BU
DELETE FROM enforcers WHERE business_unit_id = 9999;

-- Remove from prisons referencing test BU
DELETE FROM prisons WHERE business_unit_id = 9999;

-- Remove from user_entitlements referencing business_unit_users of test BU
DELETE FROM user_entitlements
WHERE business_unit_user_id IN (
    SELECT business_unit_user_id
    FROM business_unit_users
    WHERE business_unit_id = 9999
);

-- Remove from business_unit_users referencing test BU
DELETE FROM business_unit_users WHERE business_unit_id = 9999;

-- Remove from offences referencing test BU
DELETE FROM offences WHERE business_unit_id = 9999;

-- Remove from allocations referencing impositions of test creditor accounts
DELETE FROM allocations WHERE imposition_id IN (
    SELECT imposition_id FROM impositions WHERE creditor_account_id IN (
        SELECT creditor_account_id FROM creditor_accounts WHERE business_unit_id = 9999
    )
);

-- Remove from impositions referencing test creditor_accounts
DELETE FROM impositions WHERE creditor_account_id IN (
    SELECT creditor_account_id FROM creditor_accounts WHERE business_unit_id = 9999
);

-- Remove from impositions referencing test courts as imposing_court_id
DELETE FROM impositions WHERE imposing_court_id IN (
    SELECT court_id FROM courts WHERE business_unit_id = 9999
);

-- Remove from creditor_accounts referencing test BU
DELETE FROM creditor_accounts WHERE business_unit_id = 9999;

-- Remove from courts referencing test BU
DELETE FROM courts WHERE business_unit_id = 9999;

-- Remove test results if present (fix test count mismatches)
DELETE FROM results WHERE result_id IN ('TSTRES');

-- Remove from major_creditors referencing test BU
DELETE FROM major_creditors WHERE business_unit_id = 9999;

-- Remove the test business unit
DELETE FROM business_units WHERE business_unit_id = 9999;
