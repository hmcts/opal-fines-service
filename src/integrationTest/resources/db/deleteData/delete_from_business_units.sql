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
DELETE from BUSINESS_UNITS WHERE business_unit_id = 1;