/**
* OPAL Program
*
* MODULE      : enforcement_path_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table ENFORCEMENT_PATHS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 18/06/2024    I Readman    1.0         PO-390 Create the sequence to be used to generate the Primary key for the table ENFORCEMENT_PATHS
*
**/ 

CREATE SEQUENCE IF NOT EXISTS enforcement_path_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY enforcement_paths.enforcement_path_id;