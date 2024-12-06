/**
* CGI OPAL Program
*
* MODULE      : application_functions.sql
*
* DESCRIPTION : Creates the APPLICATION FUNCTIONS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 11/02/2024    A Dennis    1.0         PO-177 Creates the APPLICATION FUNCTIONS table for the Fines model
*
**/
CREATE TABLE application_functions 
(
 application_function_id     bigint         not null
,function_name               varchar(200)   not null
,CONSTRAINT application_functions_pk PRIMARY KEY 
 (
   application_function_id	
 ) 
);

COMMENT ON COLUMN application_functions.application_function_id IS 'Unique ID of this record';
COMMENT ON COLUMN application_functions.function_name IS 'Function name';
