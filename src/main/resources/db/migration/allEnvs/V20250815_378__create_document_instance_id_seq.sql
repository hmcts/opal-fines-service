/**
* CGI OPAL Program
*
* MODULE      : create_document_instance_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the DOCUMENT_INSTANCES table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------
* 30/07/2025    TMc         1.0         PO-1041 - Create the sequence to be used to generate the Primary key for the DOCUMENT_INSTANCES table.
*
**/
CREATE SEQUENCE IF NOT EXISTS document_instance_id_seq
    INCREMENT 1 
    START 60000000000000 
    MINVALUE 60000000000000
    NO MAXVALUE 
    CACHE 1
    OWNED BY document_instances.document_instance_id;