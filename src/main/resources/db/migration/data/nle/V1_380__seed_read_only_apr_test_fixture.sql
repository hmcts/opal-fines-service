/**
* OPAL Program
*
* MODULE      : seed_read_only_apr_test_fixture.sql
*
* DESCRIPTION : Create NLE seed data for the PO-10906 read-only APR Cypress test.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 23/11/2026    TT             1.0         PO-10754 - create APR test data for PO-10906
*                                          Based on original script V1_377__seed_read_only_apr_test_fixture.sql (commit b012f062a)
*                                          Brought in line with database development standard.
*
**/

DO $fixture$
DECLARE
    fixture_account_id CONSTANT bigint := 99109060000001;
    fixture_party_id CONSTANT bigint := 99109060000101;
    fixture_account_party_id CONSTANT bigint := 99109060000501;
    fixture_payment_terms_id CONSTANT bigint := 99109060000601;
    fixture_unit CONSTANT smallint := 77;
    fixture_court CONSTANT bigint := 770000000001;
BEGIN
    INSERT INTO public.defendant_accounts (
        defendant_account_id, business_unit_id, account_number, imposing_court_id, enforcing_court_id,
        amount_imposed, amount_paid, account_balance, account_status, account_type, version_number,
        originator_type, prosecutor_case_reference, fine_registration_date, last_movement_date, last_changed_date
    ) VALUES (
        fixture_account_id, fixture_unit, '10906001A', fixture_court, fixture_court,
        100.00, 0.00, 100.00, 'L', 'Fine', 1, 'NEW', 'PO10906-READONLY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ) ON CONFLICT (defendant_account_id) DO UPDATE
    SET last_movement_date = COALESCE(defendant_accounts.last_movement_date, EXCLUDED.last_movement_date),
        last_changed_date = COALESCE(defendant_accounts.last_changed_date, EXCLUDED.last_changed_date)
    WHERE defendant_accounts.business_unit_id = fixture_unit
      AND defendant_accounts.prosecutor_case_reference = 'PO10906-READONLY'
      AND (defendant_accounts.last_movement_date IS NULL OR defendant_accounts.last_changed_date IS NULL);

    INSERT INTO public.parties (party_id, organisation, surname, forenames, title, account_type, birth_date)
    VALUES (fixture_party_id, false, 'AprReadOnlyFixture', 'Jordan', 'Mr', 'Defendant', DATE '1990-01-01')
    ON CONFLICT (party_id) DO NOTHING;

    IF NOT EXISTS (
        SELECT 1 FROM public.defendant_accounts
        WHERE defendant_account_id = fixture_account_id
          AND business_unit_id = fixture_unit AND prosecutor_case_reference = 'PO10906-READONLY'
    ) OR NOT EXISTS (
        SELECT 1 FROM public.parties WHERE party_id = fixture_party_id AND surname = 'AprReadOnlyFixture'
    ) THEN
        RAISE EXCEPTION 'PO-10906 synthetic account or party ID is already in use';
    END IF;

    INSERT INTO public.defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES (fixture_account_party_id, fixture_account_id, fixture_party_id, 'Defendant', true)
    ON CONFLICT (defendant_account_party_id) DO NOTHING;

    INSERT INTO public.payment_terms (
        payment_terms_id, defendant_account_id, posted_date, terms_type_code,
        effective_date, extension, account_balance, active
    ) VALUES (fixture_payment_terms_id, fixture_account_id, CURRENT_TIMESTAMP, 'B', CURRENT_DATE, false, 100.00, true)
    ON CONFLICT (payment_terms_id) DO NOTHING;

    IF NOT EXISTS (
        SELECT 1 FROM public.defendant_account_parties
        WHERE defendant_account_party_id = fixture_account_party_id
          AND defendant_account_id = fixture_account_id AND party_id = fixture_party_id
          AND association_type = 'Defendant' AND debtor
    ) OR NOT EXISTS (
        SELECT 1 FROM public.payment_terms
        WHERE payment_terms_id = fixture_payment_terms_id
          AND defendant_account_id = fixture_account_id AND active
    ) THEN
        RAISE EXCEPTION 'PO-10906 synthetic defendant link or active payment terms are missing';
    END IF;

    INSERT INTO public.alternate_payment_references (
        defendant_account_id, business_unit_id, relationship, category, apr_text, updated_datetime, version_number
    )
    SELECT fixture_account_id, fixture_unit, 'CONSOLIDATED', 'ACC', 'APRREADONLY', CURRENT_TIMESTAMP, 0
    WHERE NOT EXISTS (
        SELECT 1 FROM public.alternate_payment_references
        WHERE defendant_account_id = fixture_account_id AND relationship = 'CONSOLIDATED'
          AND category = 'ACC' AND apr_text = 'APRREADONLY'
    );
END
$fixture$;
