/**
* OPAL Program
*
* MODULE      : delete_from_draft_accounts.sql
*
* DESCRIPTION : Cleans up the rows inserted into the DRAFT_ACCOUNTS table for Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 02/02/2026  A KHAN      1.0      PO-2292 Adds deterministic cleanup for draft_accounts.
*
*/

-- Make sure we’re operating in the expected schema
SET search_path TO public;

-- Delete rows inserted by insert_into_draft_accounts.sql
DELETE FROM public.draft_accounts
WHERE draft_account_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9);

-- Delete rows created by tests using the mocked user (token-derived values)
DELETE FROM public.draft_accounts
WHERE submitted_by = 'USER01'
   OR validated_by = 'USER01'
   OR submitted_by_name = 'normal@users.com'
   OR validated_by_name = 'normal@users.com'
   OR draft_account_id >= 100000;

-- Safety net: remove any rows tied to seeded business units if IDs drift
DELETE FROM public.draft_accounts
WHERE business_unit_id IN (73, 77, 78, 65);
