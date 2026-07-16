/**
* OPAL Program
*
* MODULE      : alter_interface_message_procedures_add_message_data.sql
*
* DESCRIPTION : Add MESSAGE_DATA handling to p_insert_interface_message and p_int_payments_in.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 23/06/2026    C Cho       1.0         PO-2618 Add MESSAGE_DATA parameter to p_insert_interface_message.
*
**/

DROP PROCEDURE IF EXISTS public.p_insert_interface_message(
    bigint,
    character varying,
    character varying,
    bigint,
    bigint,
    text
);

CREATE PROCEDURE public.p_insert_interface_message(
    IN pi_interface_job_id interface_messages.interface_job_id%TYPE,
    IN pi_message_type interface_messages.message_type%TYPE,
    IN pi_message_text interface_messages.message_text%TYPE,
    IN pi_interface_file_id interface_messages.interface_file_id%TYPE,
    IN pi_record_index interface_messages.record_index%TYPE,
    IN pi_record_detail interface_messages.record_detail%TYPE,
    IN pi_message_data interface_messages.message_data%TYPE
)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_insert_interface_message.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* PARAMETERS  : pi_interface_job_id - The job that created this message.
*             : pi_message_type     - The type of message (Exception, Error, Warning, Info).
*             : pi_message_text     - The message text or key identifying the message data.
*             : pi_interface_file_id - The file being processed when this message was created.
*             : pi_record_index     - The index of the record being processed when this message was created.
*             : pi_record_detail    - The detail from the record used for display purposes.
*             : pi_message_data     - The JSON object to be stored for summary display.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
* 23/06/2026    C Cho       2.0         PO-2618 Add MESSAGE_DATA parameter.
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
        message_text,
        message_data)
    VALUES (
        nextval('interface_message_id_seq'),
        pi_interface_job_id,
        pi_interface_file_id,
        pi_record_index,
        pi_record_detail,
        pi_message_type,
        LEFT(pi_message_text, 500),
        pi_message_data);
END;
$$;
