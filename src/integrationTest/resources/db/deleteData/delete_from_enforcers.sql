/**
* OPAL Program
*
* MODULE      : delete_from_enforcers.sql
*
* DESCRIPTION : Deletes test data for enforcers integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 11/08/2026     J.SHEEN       1.0        Delete test data for enforcers integration tests
*
**/

DELETE FROM enforcers WHERE enforcer_id = 001;

DELETE FROM business_units WHERE business_unit_id = 10059;
