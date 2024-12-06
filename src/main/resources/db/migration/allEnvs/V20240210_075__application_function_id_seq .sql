/**
* OPAL Program
*
* MODULE      : application_function_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table APPLICATION FUNCTIONS UNIT ROLES. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 10/02/2024    A Dennis    1.0         PO-177 Creates the Sequence to be used to generate the Primary key for the table APPLICATION FUNCTIONS
*
**/
CREATE SEQUENCE IF NOT EXISTS application_function_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY application_functions.application_function_id;
