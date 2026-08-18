/**
* OPAL Program
*
* DESCRIPTION : Deletes targeted business units test data after integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 20/06/2026  A Reeves    1.0      Remove business unit test data.
*
*/
DELETE FROM business_units WHERE business_unit_id = 9092;
DELETE FROM business_units WHERE business_unit_id = 9091;