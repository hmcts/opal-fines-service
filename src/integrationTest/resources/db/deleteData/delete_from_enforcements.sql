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
WHERE enforcement_id = 1;

DELETE FROM defendant_account_parties
WHERE defendant_account_id = 77;

DELETE FROM debtor_detail
WHERE party_id = 77;

DELETE FROM parties
WHERE party_id = 77;

DELETE FROM defendant_accounts
WHERE defendant_account_id = 77;

DELETE FROM courts
WHERE court_id = 1;

DELETE FROM local_justice_areas
WHERE local_justice_area_id = 240;

DELETE FROM business_units
WHERE business_unit_id = 78;