/**
* OPAL Program
*
* DESCRIPTION : Deletes till summary rows after PO-2575 integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 28/08/2026  R DODD      1.0      Remove till summary test data.
*
*/

DELETE FROM interface_messages WHERE interface_message_id IN (257501, 257502, 257503, 257504, 257505);
DELETE FROM tills WHERE till_id IN (257501, 257502, 257503, 257504);
DELETE FROM interface_files WHERE interface_file_id IN (257501, 257502, 257503, 257504);
DELETE FROM interface_jobs WHERE interface_job_id IN (257501, 257502, 257503);
DELETE FROM business_units WHERE business_unit_id IN (25750, 25751, 25752);
