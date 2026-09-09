/**
* OPAL Program
*
* DESCRIPTION : Deletes outstanding auto payment count rows after integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 25/08/2026  R DODD      1.0      Remove outstanding auto payment count test data.
*
*/

DELETE FROM tills WHERE till_id IN (247011, 247012, 247013, 247111, 247112, 247211);
DELETE FROM interface_jobs WHERE interface_job_id IN (247001, 247002, 247003, 247101, 247201);
DELETE FROM business_units WHERE business_unit_id IN (2470, 2471, 2472);
