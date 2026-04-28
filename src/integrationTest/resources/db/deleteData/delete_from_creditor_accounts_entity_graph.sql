/**
* OPAL Program
*
* MODULE      : delete_from_creditor_accounts_entity_graph.sql
*
* DESCRIPTION : Deletes Creditor Account graph data for repository integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 20/04/2026    Codex        1.0         Added creditor account entity graph integration test cleanup
*
**/

DELETE FROM creditor_accounts
WHERE creditor_account_id = 950010;

DELETE FROM major_creditors
WHERE major_creditor_id = 950001;

DELETE FROM business_units
WHERE business_unit_id = 951;
