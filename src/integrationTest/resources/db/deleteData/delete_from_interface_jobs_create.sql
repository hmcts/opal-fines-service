/**
* OPAL Program
*
* DESCRIPTION : Deletes rows created for interface job create integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 14/07/2026  R DODD      1.0      Remove interface job create test data.
*
*/

DELETE FROM interface_files
WHERE interface_job_id IN (
    SELECT interface_job_id
    FROM interface_jobs
    WHERE interface_name IN (
        'Auto Payments In Create',
        'Auto Payments In Rollback',
        'Auto Payments In Endpoint',
        'Auto Payments In Duplicate'
    )
);

DELETE FROM interface_jobs
WHERE interface_name IN (
    'Auto Payments In Create',
    'Auto Payments In Rollback',
    'Auto Payments In Endpoint',
    'Auto Payments In Duplicate'
);

DELETE FROM business_units WHERE business_unit_id = 2577;
