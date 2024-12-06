CREATE OR REPLACE PROCEDURE p_run_interface_job (
    IN pi_interface_name    interface_jobs.interface_name%TYPE,
    IN pi_business_unit_id  business_units.business_unit_id%TYPE DEFAULT NULL)
AS $$
/**
* OPAL Program
*
* MODULE      : p_run_interface_job.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
    k_msg_type_error text := 'Error';
    k_status_created text := 'Created';
    k_status_written text := 'Written';
    k_status_nodata text := 'No data';
    k_status_completed text := 'Completed';
    k_status_failed text := 'Failed';
    v_err_context text;
    v_files bigint := 0;
    v_days integer;
    v_outbound boolean;
    v_stored_procedure text;
    v_ij_id interface_jobs.interface_job_id%TYPE;
BEGIN
    -- get interface configuration
    SELECT      (item_values->>'direction' = 'outbound'),
                item_values->>'days_before_deletion',
                item_values->>'stored_procedure'
    INTO        v_outbound, v_days, v_stored_procedure
    FROM        configuration_items
    WHERE       item_name = 'INTERFACE_'||pi_interface_name;
    IF v_stored_procedure IS NULL THEN
        RAISE EXCEPTION 'Interface % not implemented', pi_interface_name;
    END IF;
    IF v_outbound THEN
        -- for outbound jobs, database creates the job and the file
        INSERT INTO interface_jobs (interface_job_id,business_unit_id, interface_name)
        VALUES      (nextval('interface_job_id_seq'),pi_business_unit_id, pi_interface_name)
        RETURNING   interface_job_id
        INTO        v_ij_id;
    ELSE
        -- for inbound, jobs and files are created by the fines service - find first available job to run
        SELECT      interface_job_id
        INTO        v_ij_id
        FROM        interface_jobs
        WHERE       interface_name = pi_interface_name AND
                    status = k_status_created AND
                    business_unit_id = COALESCE(pi_business_unit_id,business_unit_id)
        ORDER BY    created_datetime
        LIMIT 1
        FOR UPDATE SKIP LOCKED;
    END IF;
    IF v_ij_id IS NOT NULL THEN
        UPDATE  interface_jobs
        SET     started_datetime = CURRENT_TIMESTAMP
        WHERE   interface_job_id = v_ij_id;
        BEGIN
            -- call interface specific procedure - any exception in this block rollback and fall into the exception handler
            EXECUTE format('CALL %I($1)',v_stored_procedure)
            USING   v_ij_id;
            -- verify file created for an outbound interface
            IF v_outbound THEN
                SELECT  COUNT(*)
                INTO    v_files
                FROM    interface_files
                WHERE   interface_job_id = v_ij_id;
            END IF;
            -- set new job status
            UPDATE  interface_jobs
            SET     status = 
                        CASE
                            WHEN v_outbound AND v_files > 0 THEN k_status_written
                            WHEN v_outbound THEN k_status_nodata
                            ELSE k_status_completed
                        END,
                    completed_datetime = CURRENT_TIMESTAMP
            WHERE   interface_job_id = v_ij_id;
        EXCEPTION
            WHEN OTHERS THEN
                -- a failure here rolls back this block
                GET STACKED DIAGNOSTICS
                    v_err_context = PG_EXCEPTION_CONTEXT;
                UPDATE  interface_jobs
                SET     status = k_status_failed,
                        completed_datetime = CURRENT_TIMESTAMP
                WHERE   interface_job_id = v_ij_id;
                CALL p_insert_interface_message(v_ij_id,k_msg_type_error,REPLACE(sqlerrm||' - '||v_err_context,CHR(10),''));
        END;
    END IF;
    -- purge jobs here
    DELETE FROM interface_messages 
    WHERE       interface_job_id IN (
                    SELECT  interface_job_id
                    FROM    interface_jobs
                    WHERE   interface_name = pi_interface_name AND
                            status IN (k_status_completed,k_status_failed) AND
                            completed_datetime < CURRENT_DATE - v_days);
    DELETE FROM interface_files
    WHERE       interface_job_id IN (
                    SELECT  interface_job_id
                    FROM    interface_jobs
                    WHERE   interface_name = pi_interface_name AND
                            status IN (k_status_completed,k_status_failed) AND
                            completed_datetime < CURRENT_DATE - v_days);
    DELETE FROM interface_jobs
    WHERE       interface_name = pi_interface_name AND
                status IN (k_status_completed,k_status_failed) AND
                completed_datetime < CURRENT_DATE - v_days;
    COMMIT;
END;
$$ LANGUAGE plpgsql;