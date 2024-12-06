/**
* OPAL Program
*
* MODULE      : interface_file_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table INTERFACE_FILES. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    ---------    --------    ---------------------------------------------------------------------------------------------------------
* 06/06/2024    I Readman    1.0         PO-356 Creates the Sequence to be used to generate the Primary key for the table INTERFACE_FILES
*
**/
CREATE SEQUENCE IF NOT EXISTS interface_file_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY interface_files.interface_file_id;
