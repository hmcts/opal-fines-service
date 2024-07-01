/**
* OPAL Program
*
* MODULE      : control_total_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table CONTROL_TOTALS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-396 Create the sequence to be used to generate the Primary key for the table CONTROL_TOTALS
*
**/ 

CREATE SEQUENCE IF NOT EXISTS control_total_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY control_totals.control_total_id;