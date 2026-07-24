/**
* OPAL Program
*
* MODULE      : update_cash_till_report_retention_period_iso_8601.sql
*
* DESCRIPTION : Update CASH_TILL report metadata.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 20/07/2026    S Reed      1.0         PO-2607 Update cash_till retention_period and report parameters.
* 24/07/2026    S Reed      1.1         PO-2592 Move interface job status enum change into later migration.
*
**/

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_enum e
        JOIN pg_type t ON t.oid = e.enumtypid
        WHERE t.typname = 't_interface_job_status_enum'
          AND e.enumlabel = 'PROCESSED'
    ) THEN
        ALTER TYPE t_interface_job_status_enum RENAME VALUE 'PROCESSED' TO 'PROCESSING';
    END IF;
END $$;

UPDATE reports
   SET retention_period = CASE
           WHEN retention_period = '14' THEN 'P14D'
           ELSE retention_period
       END,
       report_parameters = '[
           { "name":"till_id", "prompt":"Till ID", "type":"integer", "mandatory":true, "min":1 },
           { "name":"allocated_report", "prompt":"Allocated Report", "type":"boolean", "mandatory":false }
       ]'
 WHERE report_id = 'cash_till';
