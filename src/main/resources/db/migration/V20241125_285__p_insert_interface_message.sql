CREATE OR REPLACE PROCEDURE p_insert_interface_message(
    IN pi_interface_job_id interface_jobs.interface_job_id%TYPE,
    IN pi_message_type interface_messages.message_type%TYPE,
    IN pi_message_text interface_messages.message_text%TYPE,
    IN pi_interface_file_id interface_files.interface_file_id%TYPE DEFAULT NULL,
    IN pi_record_index interface_messages.record_index%TYPE DEFAULT NULL,
    IN pi_record_detail interface_messages.record_detail%TYPE DEFAULT NULL)
AS $$
/**
* OPAL Program
*
* MODULE      : p_insert_interface_message.sql
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
BEGIN
    INSERT INTO interface_messages (
        interface_message_id,
        interface_job_id,
        interface_file_id,
        record_index,
        record_detail,
        message_type,
        message_text)
    VALUES (
        nextval('interface_message_id_seq'),
        pi_interface_job_id,
        pi_interface_file_id,
        pi_record_index,
        pi_record_detail,
        pi_message_type,
        LEFT(pi_message_text,500));
END;
$$ LANGUAGE plpgsql;
