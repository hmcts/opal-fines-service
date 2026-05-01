/**
* OPAL Program
*
* MODULE      : delete_from_result_entity_graph.sql
*
* DESCRIPTION : Deletes targeted result entity graph test data after repository integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 27/04/2026  Dat Nguyen       1.0      Remove result data used to verify lazy loading and entity graph fetch behaviour.
*
*/

DELETE FROM enforcements WHERE enforcement_id = 920001;
DELETE FROM defendant_accounts WHERE defendant_account_id = 920002;
DELETE FROM results WHERE result_id = 'RG9200';
DELETE FROM enforcers WHERE enforcer_id = 920003;
DELETE FROM courts WHERE court_id = 920004;
DELETE FROM local_justice_areas WHERE local_justice_area_id = 920;
DELETE FROM business_units WHERE business_unit_id = 920;
