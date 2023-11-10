/**
* OPAL Program
*
* MODULE      : note_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table NOTES. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 09/11/2023    A Dennis    1.0         PO-39 Creates the Sequence to be used to generate the Primary key for the table NOTES
*
**/
CREATE SEQUENCE note_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY notes.note_id;
