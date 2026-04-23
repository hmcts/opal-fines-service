/**
* OPAL Program
*
* MODULE      : delete_from_enforcements_entity_graph.sql
*
* DESCRIPTION : Deletes targeted enforcement entity graph test data after repository integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 16/04/2026  S WILLIAMS  1.0      PO-2883: Remove enforcement data used to verify lazy loading and entity graph fetch behaviour.
*
*/

DELETE FROM enforcements WHERE enforcement_id = 910001;
DELETE FROM defendant_accounts WHERE defendant_account_id = 910002;
DELETE FROM results WHERE result_id = 'ER9100';
DELETE FROM enforcers WHERE enforcer_id = 910003;
DELETE FROM courts WHERE court_id = 910004;
DELETE FROM local_justice_areas WHERE local_justice_area_id = 910;
DELETE FROM business_units WHERE business_unit_id = 910;
