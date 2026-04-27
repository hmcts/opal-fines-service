/**
* OPAL Program
*
* MODULE      : delete_from_impositions_entity_graph.sql
*
* DESCRIPTION : Deletes Imposition entity graph data for integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 17/04/2026    S WILLIAMS   1.0         PO-2884 Delete rows of data from IMPOSITIONS and related tables for integration tests
*
**/

DELETE FROM impositions WHERE imposition_id = 551005;
DELETE FROM defendant_accounts WHERE defendant_account_id = 551002;
DELETE FROM creditor_accounts WHERE creditor_account_id = 551004;
DELETE FROM major_creditors WHERE major_creditor_id = 551003;
DELETE FROM offences WHERE offence_id = 5510;
DELETE FROM results WHERE result_id = 'IGR001';
DELETE FROM courts WHERE court_id = 551001;
DELETE FROM local_justice_areas WHERE local_justice_area_id = 5501;
DELETE FROM business_units WHERE business_unit_id = 55;
