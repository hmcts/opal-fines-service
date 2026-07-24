/**
* OPAL Program
*
* MODULE      : alter_interface_jobs_status_to_enum.sql
*
* DESCRIPTION : Alter interface_jobs.status to use PostgreSQL enum, drop check constraint and update column default
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3624 - Update columns on INTERFACE_JOBS table to use PostgreSQL ENUM
*
**/

ALTER TABLE interface_jobs
    DROP CONSTRAINT IF EXISTS interface_jobs_status_cc;

ALTER TABLE interface_jobs
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE interface_jobs
    ALTER COLUMN status TYPE t_interface_job_status_enum
    USING CASE status::text
            WHEN 'Created' THEN 'CREATED'::t_interface_job_status_enum
            WHEN 'Written' THEN 'PROCESSED'::t_interface_job_status_enum
            WHEN 'No data' THEN 'IGNORED'::t_interface_job_status_enum
            WHEN 'Completed' THEN 'COMPLETED'::t_interface_job_status_enum
            WHEN 'Failed' THEN 'FAILED'::t_interface_job_status_enum
            ELSE status::t_interface_job_status_enum
          END;

ALTER TABLE interface_jobs
    ALTER COLUMN status SET DEFAULT 'CREATED'::t_interface_job_status_enum;

COMMENT ON COLUMN interface_jobs.status IS 'The status of this interface job. Specific values can be found in the DB LLD on Confluence.';

DROP INDEX IF EXISTS ij_status_created_idx;

CREATE INDEX ij_status_created_idx
    ON interface_jobs (status, interface_name, created_datetime);
