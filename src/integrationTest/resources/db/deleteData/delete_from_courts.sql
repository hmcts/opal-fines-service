/**
* OPAL Program
*
* MODULE      : delete_from_courts.sql
*
* DESCRIPTION : Deletes test data from courts table for the Integration Tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 11/08/2026     J.SHEEN       1.0        Delete test data for courts integration tests
*
**/

DELETE FROM courts WHERE court_id = 000000000007;

DELETE FROM business_units WHERE business_unit_id = 991;
