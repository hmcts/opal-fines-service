/**
* OPAL Program
*
* MODULE      : message_log.sql
*
* DESCRIPTION : Create the MESSAGE_LOG table.
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    -------------------------------------
* 08/10/2024    I Readman    1.0         PO-665 Create the MESSAGE_LOG table
*
**/

CREATE TABLE message_log
(
 message_log_id             bigint          not null
,message_uuid               varchar(50)
,created_date               timestamp       not null  
,procedure_name             varchar(40)
,error_message              text
,additional_information     text
,CONSTRAINT ml_message_log_id_pk PRIMARY KEY (message_log_id)
);

CREATE INDEX ml_created_date_idx ON message_log (created_date);

CREATE SEQUENCE IF NOT EXISTS message_log_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY message_log.message_log_id;

COMMENT ON COLUMN message_log.message_log_id IS 'ID of the error message being logged';
COMMENT ON COLUMN message_log.message_uuid IS 'The universally unique identifier that will tie the request sent from the frontend or backend to the error message being stored in the database. This can be displayed on the frontend for the user to pass on to Admin for investigation';
COMMENT ON COLUMN message_log.created_date IS 'Date the message was created in the table';
COMMENT ON COLUMN message_log.procedure_name IS 'If a database procedure or function was used then record the name';
COMMENT ON COLUMN message_log.error_message IS 'The error message that is being stored';
COMMENT ON COLUMN message_log.additional_information IS 'Any information that could be helpful to investigate the cause of failure';
