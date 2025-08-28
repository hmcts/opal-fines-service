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
WHERE creditor_transaction_id = 90001;

-- Delete test creditor accounts
DELETE FROM public.creditor_accounts
WHERE creditor_account_id IN (104, 105);

-- Delete test creditor party
DELETE FROM public.parties
WHERE party_id = 9000;



