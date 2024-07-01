/**
* OPAL Program
*
* MODULE      : standard_letter_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table STANDARD_LETTERS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-398 Create the sequence to be used to generate the Primary key for the table STANDARD_LETTERS
*
**/ 

CREATE SEQUENCE IF NOT EXISTS standard_letter_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY standard_letters.standard_letter_id;