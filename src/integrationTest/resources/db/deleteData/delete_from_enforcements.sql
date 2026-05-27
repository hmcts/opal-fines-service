/**
* OPAL Program
*
* MODULE      : delete_from_amendments.sql
*
* DESCRIPTION : Cleans up the rows inserted into the enforcements table for the Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author   Version  Nature of Change
* ----------  -------  -------  -------------------------------------------------------------
* 01/05/2026  S WALL   1.0      PO-2255 Deletes rows of data from the ENFORCEMENTS tables.
*
*/

SET search_path TO public;

DELETE FROM enforcements
WHERE defendant_account_id = 77;

DELETE FROM defendant_account_parties
WHERE defendant_account_id = 77;

DELETE FROM debtor_detail
WHERE party_id = 77;

DELETE FROM payment_terms
WHERE defendant_account_id = 77;

DELETE FROM impositions
WHERE defendant_account_id = 77;

DELETE FROM creditor_accounts
WHERE creditor_account_id = 1;

DELETE FROM defendant_transactions
WHERE defendant_account_id = 77;

DELETE FROM defendant_accounts
WHERE defendant_account_id = 77;

DELETE FROM defendant_accounts
WHERE defendant_account_id IN (77, 78);

DELETE FROM parties
WHERE party_id = 77;