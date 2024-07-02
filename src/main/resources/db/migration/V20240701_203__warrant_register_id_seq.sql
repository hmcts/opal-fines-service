/**
* OPAL Program
*
* MODULE      : warrant_register_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table WARRANT_REGISTER. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-397 Create the sequence to be used to generate the Primary key for the table WARRANT_REGISTER
*
**/ 

CREATE SEQUENCE IF NOT EXISTS warrant_register_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY warrant_register.warrant_register_id;