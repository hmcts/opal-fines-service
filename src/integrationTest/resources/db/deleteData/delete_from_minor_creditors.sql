/**
* OPAL Program
*
* MODULE      : delete_from_minor_creditors.sql
*
* DESCRIPTION : Cleans up the rows inserted into the minor_creditor tables for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 27/08/2025  M Mollins   1.0      PO-713 Deletes rows of data from the MINOR_CREDITORS tables.
*
*/

-- Delete test creditor transactions first (FK references creditor_accounts)
DELETE FROM public.creditor_transactions
WHERE creditor_account_id IN (104, 606);

-- Delete test impositions first (FK references creditor_accounts, defendant_accounts)
DELETE FROM public.impositions
WHERE creditor_account_id = 606;

-- Delete test creditor accounts
DELETE FROM public.creditor_accounts
WHERE creditor_account_id IN (104, 105, 999950, 999951, 999952, 999953, 999954, 999955, 606, 607);

-- Delete test creditor parties
DELETE FROM public.parties
WHERE party_id IN (9000, 9001, 9002, 9003, 9004, 9005, 9006, 99007, 99008);

-- Delete test defendant accounts
DELETE FROM public.defendant_accounts
WHERE defendant_account_id = 70000000000000;
