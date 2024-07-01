/**
* OPAL Program
*
* MODULE      : cheque_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table CHEQUES. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-394 Create the sequence to be used to generate the Primary key for the table CHEQUES
*
**/ 

CREATE SEQUENCE IF NOT EXISTS cheque_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY cheques.cheque_id;