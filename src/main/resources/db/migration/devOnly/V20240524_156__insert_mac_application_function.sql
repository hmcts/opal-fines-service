/**
* OPAL Program
*
* MODULE      : insert_mac_application_function.sql
*
* DESCRIPTION : Inserts the Manual Account Creation row into the APPLICATION FUNCTIONS table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------
* 24/05/2024    A Dennis    1.0         PO-380 Inserts the Manual Account Creation row into the APPLICATION FUNCTIONS table.
**/
INSERT INTO application_functions
(               
 application_function_id                 
,function_name                                       
)
VALUES
(
 35
,'Manual Account Creation'
);
