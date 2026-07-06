/**
* OPAL Program
*
* MODULE      : alter_print_job_status_to_enum.sql
*
* DESCRIPTION : Alter print_job.status to use PostgreSQL enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3843 - Update columns on PRINT_JOB table to use PostgreSQL ENUM
*
**/

ALTER TABLE print_job
    DROP CONSTRAINT IF EXISTS print_job_status_check;

ALTER TABLE print_job
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE print_job
    ALTER COLUMN status TYPE t_print_job_status_enum
    USING status::text::t_print_job_status_enum;

ALTER TABLE print_job
    ALTER COLUMN status SET DEFAULT 'PENDING'::t_print_job_status_enum;

DROP INDEX IF EXISTS idx_status;

CREATE INDEX pj_status_idx ON print_job (status);
