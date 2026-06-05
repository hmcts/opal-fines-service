-- Account 22005 (BU 78)
INSERT INTO defendant_accounts
( defendant_account_id, version_number, business_unit_id, account_number,
  amount_paid, account_balance, amount_imposed, account_status,
  prosecutor_case_reference, allow_writeoffs, allow_cheques, account_type,
  collection_order, payment_card_requested )
VALUES (22005, 0, 78, '22005A',
        0.00, 0.00, 0.00, 'L',
        '22005PCR', 'N', 'N', 'Fine',
        'N', 'N'),
       (22006, 0, 78, '22006A',
        0.00, 0.00, 0.00, 'L',
        '22006PCR', 'N', 'N', 'Fine',
        'N', 'N')
  ON CONFLICT (defendant_account_id) DO NOTHING;

-- Party 22005 (individual)
INSERT INTO parties
( party_id, organisation, organisation_name,
  surname, forenames, title,
  address_line_1, postcode, birth_date, national_insurance_number, last_changed_date )
VALUES (22005, 'N', NULL,
        'SeedSurname22005', 'SeedForenames22005', 'Mr',
        'Seed Address 22005', 'SE2 0AA', '1990-01-01 00:00:00', 'SNI22005', NULL),
       (22006, 'Y', 'Seed Org',
        NULL, NULL, NULL,
        'Seed Address 22006', 'SE2 0AA', NULL, NULL, NULL)
  ON CONFLICT (party_id) DO NOTHING;

-- DAP link
INSERT INTO defendant_account_parties
( defendant_account_party_id, defendant_account_id, party_id, association_type, debtor )
VALUES (22005, 22005, 22005, 'Defendant', 'Y'),
       (22006, 22006, 22006, 'Defendant', 'Y')
  ON CONFLICT (defendant_account_party_id) DO NOTHING;

-- Minimal debtor_detail
INSERT INTO debtor_detail
( party_id, vehicle_make, vehicle_registration,
  employer_name, employer_address_line_1, employer_postcode,
  employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date )
VALUES
  ( 22005, NULL, NULL,
    NULL, NULL, NULL,
    NULL, NULL, NULL,
    NULL, NULL, NULL, NULL ),
  ( 22006, NULL, NULL,
    NULL, NULL, NULL,
    NULL, NULL, NULL,
    NULL, NULL, NULL, NULL )
  ON CONFLICT (party_id) DO NOTHING;

-- Seed one individual alias we will update in the test
INSERT INTO aliases (alias_id, party_id, surname, forenames, sequence_number, organisation_name)
VALUES
  (2200501, 22005, 'AliasSurnameSeed', 'AliasForenamesSeed', 1, NULL),
  (2200502, 22005, 'AliasSurnameSeed', 'AliasForenamesSeed', 2, NULL)
  ON CONFLICT (alias_id) DO NOTHING;
