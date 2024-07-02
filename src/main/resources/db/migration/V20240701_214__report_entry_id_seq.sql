/**
* OPAL Program
*
* MODULE      : report_entry_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table REPORT_ENTRIES. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-403 Create the sequence to be used to generate the Primary key for the table REPORT_ENTRIES
*
**/ 

CREATE SEQUENCE IF NOT EXISTS report_entry_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY report_entries.report_entry_id;   