/**
* OPAL Program
*
* MODULE      : delete_from_draft_accounts_vpfpo.sql
*
* DESCRIPTION : Cleans up the rows inserted into the DRAFT_ACCOUNTS table for the VPFPO Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 20/10/2026  T Croasdale 1.0      Adds cleanup for draft_accounts.
*
*/

-- Make sure we’re operating in the expected schema
SET search_path TO public;

-- Delete rows inserted by insert_into_draft_accounts.sql
DELETE FROM public.draft_accounts
WHERE draft_account_id IN (11, 12, 21, 22);

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
