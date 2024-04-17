CREATE OR REPLACE PROCEDURE delete_expired_log_audit()
LANGUAGE 'plpgsql'

AS 
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : delete_expired_log_audit.sql
*
* DESCRIPTION : Procedure to physically delete log and audit data from the log_audit_details table after their retention time.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------
* 11/04/2024    A Dennis    1.0         PO-238 Procedure to physically delete log and audit data from the log_audit_details table after their retention time.
*
**/
DECLARE

    v_log_audit_deletion_time     INTEGER;
    v_deletion_time_string        VARCHAR(20);
	
BEGIN

    -- Get the retention time for Log and Audit data
    SELECT item_value
    INTO   v_log_audit_deletion_time
    FROM   configuration_items
    WHERE  item_name = 'AUDIT_LOG_RETENTION_PERIOD_DAYS';

    v_deletion_time_string := v_log_audit_deletion_time||' days';

    -- Delete log audit data that have exceeded their retention time.
    DELETE FROM log_audit_details 
    WHERE date_trunc('day', LOCALTIMESTAMP) > date_trunc('day', log_timestamp + v_deletion_time_string::INTERVAL);

    -- No exceptions error handling here so that if any failures occur the application layer that calls it will report it in the logs.
        		
END;
$BODY$;

COMMENT ON PROCEDURE delete_expired_log_audit
    IS 'Procedure to physically delete log and audit data from the log_audit_details table after their retention time';