-- Delete company/individual aliases (AC9d/AC9di and others)
DELETE FROM aliases
WHERE alias_id IN (
                   7701, 7702, 7703,         -- aliases for party 77 (individual)
                   8801, 9011,               -- existing (88, 901)
                   5551, 5552, 6661, 6662, 7771,
                   10001, 10002, 10003, 10004, 10005, 100011, 100012, 100013,
                   200101, 200102,           -- org aliases for party 20010 (NEW)
                   2200401                   -- individual alias for party 22004 (NEW)
    );

-- Also cover any aliases by party for full safety
DELETE FROM aliases
WHERE party_id IN (
                   77, 88, 901, 333, 555, 666, 777, 444, 999, 77444, 10001, 10002, 10003,
                   20010, 22004              -- NEW: party used by individual-aliases IT
    );

-- Remove defendant_account_parties links
DELETE FROM defendant_account_parties
WHERE defendant_account_party_id IN (
                                     77, 78, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 9077, 77444,
                                     20010, 22004         -- NEW
    );

-- Remove from fixed_penalty_offences (safe even if none exist)
DELETE FROM fixed_penalty_offences
WHERE defendant_account_id IN (
                               77, 88, 901, 333, 555, 666, 777, 444, 999, 9077, 77444,
                               20010, 22004     -- NEW
    );

-- Remove payment_terms (safe even if none exist)
DELETE FROM payment_terms
WHERE defendant_account_id IN (
                               77, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 9077, 77444,
                               20010, 22004     -- NEW
    );

-- Remove notes (ASSOCIATED_RECORD_ID is varchar)
DELETE FROM notes
WHERE associated_record_id IN (
                               '77', '88', '901', '333', '555', '666', '777', '444', '999', '9077', '77444',
                               '20010', '22004'   -- NEW
    );

DELETE FROM enforcements
WHERE defendant_account_id IN ( 78 );

-- Reset PCR-related fields to stable test values BEFORE deleting accounts
UPDATE defendant_accounts
SET
    version_number = 0,
    payment_card_requested = false,
    payment_card_requested_by = NULL,
    payment_card_requested_by_name = NULL,
    payment_card_requested_date = '2024-01-01'
WHERE defendant_account_id IN (
                               77, 88, 901, 333, 555, 666, 777, 444, 999,
                               10001, 10002, 10003, 10004, 9077, 77444
    );

-- Remove payment card requests created during tests
DELETE FROM payment_card_requests
WHERE defendant_account_id IN (77, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 9077, 77444);

-- Remove main defendant accounts
DELETE FROM defendant_accounts
WHERE defendant_account_id IN (
                               77, 78, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 9077, 77444,
                               20010, 22004       -- NEW
    );

-- Remove from debtor_detail before removing parties
DELETE FROM debtor_detail
WHERE party_id IN (
                   77, 78, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 77444,
                   20010, 22004     -- NEW
    );

-- Remove inserted parties
DELETE FROM parties
WHERE party_id IN (
                   77, 78, 88, 901, 333, 555, 666, 777, 444, 999, 10001, 10002, 10003, 10004, 77444,
                   20010, 22004     -- NEW
    );

-- Remove from draft_accounts referencing test BU
DELETE FROM draft_accounts WHERE business_unit_id = 9999;

-- Remove from enforcers referencing test BU
DELETE FROM enforcers WHERE business_unit_id = 9999;

-- Remove from prisons referencing test BU
DELETE FROM prisons WHERE business_unit_id = 9999;

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
DELETE FROM result_documents WHERE result_id IN ('TTPAY');

-- Remove test results if present (fix test count mismatches)
DELETE FROM results WHERE result_id IN ('TSTRES');

-- Remove from major_creditors referencing test BU
DELETE FROM major_creditors WHERE business_unit_id = 9999;

-- Remove the test business unit
DELETE FROM business_units WHERE business_unit_id = 9999;

-- -- Ensure table exists so delete is a no-op if absent
-- CREATE TABLE IF NOT EXISTS public.enforcement_override_results (
--                                                                    enforcement_override_result_id   varchar(50) PRIMARY KEY,
--                                                                    enforcement_override_result_name varchar(200) NOT NULL
-- );
--
-- -- Remove test enforcement-override result used by ITs
-- DELETE FROM public.enforcement_override_results
-- WHERE enforcement_override_result_id IN ('FWEC');

-- Remove test Local Justice Area used by ITs
DELETE FROM local_justice_areas
WHERE local_justice_area_id IN (240);

-- Remove test Enforcers used by ITs
DELETE FROM enforcers
WHERE enforcer_id IN (780000000021, 21)
   OR (business_unit_id = 78 AND (enforcer_code = 21 OR name = 'North East Enforcement'));

-- Remove test Courts used by ITs
DELETE FROM courts
WHERE court_id IN (780000000185, 100);

-- Remove test document instances used by ITs
DELETE FROM document_instances
WHERE document_id IN ('TTPLET');

-- Remove test document templates used by ITs
DELETE FROM documents
WHERE document_id IN ('TTPLET');