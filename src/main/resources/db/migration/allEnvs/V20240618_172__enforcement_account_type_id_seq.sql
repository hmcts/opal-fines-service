/**
* OPAL Program
*
* MODULE      : enforcement_account_type_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table ENFORCEMENT_ACCOUNT_TYPES. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 18/06/2024    I Readman    1.0         PO-390 Create the sequence to be used to generate the Primary key for the table ENFORCEMENT_ACCOUNT_TYPES
*
**/ 

CREATE SEQUENCE IF NOT EXISTS enforcement_account_type_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY enforcement_account_types.enforcement_account_type_id; 