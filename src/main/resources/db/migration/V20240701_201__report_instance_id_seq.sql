/**
* OPAL Program
*
* MODULE      : report_instance_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table REPORT_INSTANCES. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-401 Create the sequence to be used to generate the Primary key for the table REPORT_INSTANCES
*
**/ 

CREATE SEQUENCE IF NOT EXISTS report_instance_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY report_instances.report_instance_id; 