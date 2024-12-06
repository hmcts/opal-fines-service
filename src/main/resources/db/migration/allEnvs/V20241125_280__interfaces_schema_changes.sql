/**
* OPAL Program
*
* MODULE      : interfaces_schema_changes.sql
*
* DESCRIPTION : This script was written by Capita. It carries out Database schema changes required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This script was written by Capita. It carries out Database schema changes required for interface files
*
**/
/* TABLE tills */
ALTER TABLE tills
    ALTER COLUMN owned_by DROP NOT NULL;
-- sequence
CREATE SEQUENCE IF NOT EXISTS till_id_seq INCREMENT 1 START WITH 1 MINVALUE 1 NO MAXVALUE CACHE 20;
ALTER SEQUENCE till_id_seq OWNED BY tills.till_id;
/* Create a till number Sequence 10000-99999 per accounting division to satisfy exeisting functionality */
DO $$
DECLARE
    v_bu_id business_units.business_unit_id%TYPE;
    v_cmd text := 'DROP SEQUENCE IF EXISTS till_number_?_seq; CREATE SEQUENCE till_number_?_seq INCREMENT 1 START 10000 MINVALUE 10000 MAXVALUE 99999 CYCLE CACHE 1;';
BEGIN
    FOR v_bu_id IN (SELECT business_unit_id FROM business_units WHERE business_unit_type = 'Accounting Division')
    LOOP
        EXECUTE REPLACE(v_cmd,'?',v_bu_id::text);
    END LOOP;
END $$;

/* TABLE payments_in */
ALTER TABLE payments_in
    DROP COLUMN IF EXISTS auto_payment,
    ADD COLUMN auto_payment boolean;
-- index
CREATE INDEX IF NOT EXISTS pi_till_id_idx ON payments_in (till_id);
-- sequence
CREATE SEQUENCE IF NOT EXISTS payment_in_id_seq INCREMENT 1 START WITH 1 MINVALUE 1 NO MAXVALUE NO CYCLE CACHE 20;
ALTER SEQUENCE payment_in_id_seq OWNED BY payments_in.payment_in_id;

/* TABLE interface_jobs */
DROP TABLE IF EXISTS interface_jobs CASCADE;
CREATE TABLE IF NOT EXISTS interface_jobs
(
    interface_job_id bigint NOT NULL,
    business_unit_id smallint,
    interface_name character varying(50) NOT NULL,
    status character varying(10) NOT NULL DEFAULT 'Created',
    created_datetime timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_datetime timestamp without time zone,
    completed_datetime timestamp without time zone,
    CONSTRAINT interface_jobs_pk PRIMARY KEY (interface_job_id),
    CONSTRAINT if_business_unit_id_fk FOREIGN KEY (business_unit_id)
        REFERENCES business_units (business_unit_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT interface_jobs_status_cc 
	CHECK (status IN ('Created','Written','No data','Completed','Failed'))
);
COMMENT ON COLUMN interface_jobs.interface_job_id
    IS 'Primary key';
COMMENT ON COLUMN interface_jobs.interface_name
    IS 'The name of the procedure that will process records for this job';
COMMENT ON COLUMN interface_jobs.status
    IS 'The status of this interface job (Created, Completed, Failed)';
COMMENT ON COLUMN interface_jobs.created_datetime
    IS 'The timestamp when this record was created';
COMMENT ON COLUMN interface_jobs.started_datetime
    IS 'The timestamp when the procedure started';
COMMENT ON COLUMN interface_jobs.completed_datetime
    IS 'The timestamp when the procedure completed of failed';
-- indexes
CREATE INDEX ij_status_created_idx ON interface_jobs(status, interface_name, created_datetime);
-- sequence
CREATE SEQUENCE IF NOT EXISTS interface_job_id_seq INCREMENT 1 START WITH 1 MINVALUE 1 NO MAXVALUE CACHE 20;
ALTER SEQUENCE interface_job_id_seq OWNED BY interface_jobs.interface_job_id;

/* TABLE interface_files */
DROP TABLE IF EXISTS interface_files CASCADE;
CREATE TABLE IF NOT EXISTS interface_files
(
    interface_file_id bigint NOT NULL,
    interface_job_id bigint NOT NULL,
    file_name character varying(200) NOT NULL,
    created_datetime timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    records json,
    CONSTRAINT interface_files_pk PRIMARY KEY (interface_file_id),
    CONSTRAINT if_interface_job_id_fk FOREIGN KEY (interface_job_id)
        REFERENCES interface_jobs (interface_job_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
COMMENT ON COLUMN interface_files.interface_file_id
    IS 'Primary key';
COMMENT ON COLUMN interface_files.interface_job_id
    IS 'The job responsible for either creating this file (exports) or processing the file (imports)';
COMMENT ON COLUMN interface_files.file_name
    IS 'The name and location of the stored file';
COMMENT ON COLUMN interface_files.created_datetime
    IS 'The timestamp when this record was created';
COMMENT ON COLUMN interface_files.records
    IS 'An array of JSON objects each representing a record in the file';
-- indexes
CREATE INDEX IF NOT EXISTS if_interface_job_id_idx ON interface_files(interface_job_id);
-- sequence
CREATE SEQUENCE IF NOT EXISTS interface_file_id_seq INCREMENT 1 START WITH 1 MINVALUE 1 NO MAXVALUE CACHE 20; 
ALTER SEQUENCE interface_file_id_seq OWNED BY interface_files.interface_file_id;

/* TABLE interface_messages */
DROP TABLE IF EXISTS interface_messages CASCADE;
CREATE TABLE IF NOT EXISTS interface_messages
(
    interface_message_id bigint NOT NULL,
    interface_job_id bigint NOT NULL,
    interface_file_id bigint,
    message_type character varying(10) NOT NULL,
    message_text character varying(500) NOT NULL,
    record_index bigint,
    record_detail text,
    CONSTRAINT interface_messages_pk PRIMARY KEY (interface_message_id),
    CONSTRAINT im_interface_file_id_fk FOREIGN KEY (interface_file_id)
        REFERENCES interface_files (interface_file_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT im_interface_job_id_fk FOREIGN KEY (interface_job_id)
        REFERENCES interface_jobs (interface_job_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT im_message_type_cc
	    CHECK (message_type IN ('Error','Exception','Warning','Info'))
);
COMMENT ON COLUMN interface_messages.interface_message_id
    IS 'Primary key';
COMMENT ON COLUMN interface_messages.interface_job_id
    IS 'The job that created this message';
COMMENT ON COLUMN interface_messages.interface_file_id
    IS 'The file (if any) being processed when this message was created';
COMMENT ON COLUMN interface_messages.record_index
    IS 'The index of the record from the array being processed (if applicable) when this message was created';
COMMENT ON COLUMN interface_messages.record_detail
    IS 'The detail from the record used for display purposes';
COMMENT ON COLUMN interface_messages.message_type
    IS 'The type of message (Exception, Error, Warning, Info)';
COMMENT ON COLUMN interface_messages.message_text
    IS 'The message text to be displayed';
-- indexes
CREATE INDEX IF NOT EXISTS im_interface_job_id_idx ON interface_messages(interface_job_id);
CREATE INDEX IF NOT EXISTS im_interface_file_id_idx ON interface_messages(interface_file_id);
-- sequence
CREATE SEQUENCE IF NOT EXISTS interface_message_id_seq INCREMENT 1 START WITH 1 MINVALUE 1 NO MAXVALUE CACHE 20;
ALTER SEQUENCE interface_message_id_seq OWNED BY interface_messages.interface_message_id;

/* TABLE payment_card_requests */
DROP TABLE IF EXISTS payment_card_requests;
CREATE TABLE payment_card_requests (
    defendant_account_id bigint NOT NULL,
    CONSTRAINT payment_card_requests_pk PRIMARY KEY (defendant_account_id),
    CONSTRAINT pcr_defendant_account_id_fk FOREIGN KEY (defendant_account_id)
        REFERENCES defendant_accounts (defendant_account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
COMMENT ON COLUMN payment_card_requests.defendant_account_id
    IS 'Primary key. Indicates a request has been made for this defendant account. Deleted once processed';
