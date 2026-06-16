/**
* CGI OPAL Program
*
* MODULE      : seed_defendant_account_history_amendment.sql
*
* DESCRIPTION : Add a deterministic amendment row for the seeded defendant-account history
*               functional tests.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------------------------------------------
* 16/06/2026    C Cho       1.0         PO-2622 Seed mixed defendant-account history data for functional tests
*
**/

INSERT INTO public.amendments (
    amendment_id,
    business_unit_id,
    associated_record_type,
    associated_record_id,
    amended_date,
    amended_by,
    field_code,
    old_value,
    new_value,
    case_reference,
    function_code,
    amended_by_name
) VALUES (
    99000000014001,
    77,
    'defendant_accounts',
    '99000000000001',
    '2026-05-13 11:37:55.196682',
    'L080JG',
    1,
    'Original major creditor',
    'Updated major creditor',
    'CASE-HIST-00001',
    'UPDATE',
    'opal-test'
) ON CONFLICT (amendment_id) DO NOTHING;
