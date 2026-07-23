/**
* OPAL Program
*
* DESCRIPTION : Deletes interface job summary rows after integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 02/07/2026  R DODD      1.0      Remove interface job summary test data.
*
*/

DELETE FROM interface_files WHERE interface_file_id IN (257411, 257412, 257413, 257414, 257415);
DELETE FROM interface_jobs WHERE interface_job_id IN (257401, 257402, 257403, 257404);
DELETE FROM business_units WHERE business_unit_id IN (2574, 2575);
