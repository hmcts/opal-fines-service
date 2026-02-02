/**
* OPAL Program
*
* MODULE      : update_into_parties.sql
*
* DESCRIPTION : Inserts rows of data into the DEFENDANT_ACCOUNTS table for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author   Version  Nature of Change
* ----------  -------  -------  -----------------------------------------------------------------------------------------
* 28/01/2026  R DODD   1.0      PO-2296: 'Reset' Party data for some tests that re-use it.
*
**/

-- Make sure we’re operating in the expected schema
-- SET search_path TO public;

-- Party linked to the account (starts as organisation=false → your PUT can set org=true if you like)

INSERT INTO parties (
    party_id, organisation, organisation_name,
    surname, forenames, title,
    address_line_1, postcode, birth_date,
    national_insurance_number, last_changed_date
)
VALUES (
           20010, 'N', NULL,
           'SeedSurname', 'SeedForenames', 'Mr',
           'Seed Address', 'SE1 1ED',
           '1980-01-01 00:00:00', 'SEEDNI10', NULL
       )
ON CONFLICT (party_id) DO UPDATE
    SET
        organisation               = EXCLUDED.organisation,
        organisation_name          = EXCLUDED.organisation_name,
        surname                    = EXCLUDED.surname,
        forenames                  = EXCLUDED.forenames,
        title                      = EXCLUDED.title,
        address_line_1             = EXCLUDED.address_line_1,
        postcode                   = EXCLUDED.postcode,
        birth_date                 = EXCLUDED.birth_date,
        national_insurance_number  = EXCLUDED.national_insurance_number,
        last_changed_date          = NOW();  -- stamp the update time


INSERT INTO debtor_detail (
    party_id,
    vehicle_make, vehicle_registration,
    employer_name, employer_address_line_1, employer_postcode,
    employee_reference, employer_telephone, employer_email,
    document_language, document_language_date,
    hearing_language, hearing_language_date
)
VALUES (
           20010,
           NULL, NULL,
           NULL, NULL, NULL,
           NULL, NULL, NULL,
           NULL, NULL,
           NULL, NULL
       )
ON CONFLICT (party_id) DO UPDATE
    SET
        vehicle_make            = EXCLUDED.vehicle_make,
        vehicle_registration    = EXCLUDED.vehicle_registration,
        employer_name           = EXCLUDED.employer_name,
        employer_address_line_1 = EXCLUDED.employer_address_line_1,
        employer_postcode       = EXCLUDED.employer_postcode,
        employee_reference      = EXCLUDED.employee_reference,
        employer_telephone      = EXCLUDED.employer_telephone,
        employer_email          = EXCLUDED.employer_email,
        document_language       = EXCLUDED.document_language,
        document_language_date  = EXCLUDED.document_language_date,
        hearing_language        = EXCLUDED.hearing_language,
        hearing_language_date   = EXCLUDED.hearing_language_date;


INSERT INTO aliases (
    alias_id,
    party_id,
    surname,
    forenames,
    sequence_number,
    organisation_name
)
VALUES
    (200101, 20010, NULL, NULL, 1, 'Seed Org Alias 1'),
    (200102, 20010, NULL, NULL, 2, 'Seed Org Alias 2')
ON CONFLICT (alias_id) DO UPDATE
    SET
        party_id         = EXCLUDED.party_id,
        surname          = EXCLUDED.surname,
        forenames        = EXCLUDED.forenames,
        sequence_number  = EXCLUDED.sequence_number,
        organisation_name = EXCLUDED.organisation_name;

