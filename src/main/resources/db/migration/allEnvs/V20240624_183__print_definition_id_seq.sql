/**
* OPAL Program
*
* MODULE      : print_definition_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table PRINT_DEFINITION. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 05/03/2024    A Dennis    1.0         PO-208 Creates the Sequence to be used to generate the Primary key for the table PRINT_DEFINITION
*
**/
CREATE SEQUENCE IF NOT EXISTS print_definition_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY print_definition.print_definition_id;
