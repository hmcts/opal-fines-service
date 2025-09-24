/**
* CGI OPAL Program
*
*
* DESCRIPTION : Load the AMENDMENTS table with reference data for the Integration Tests
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 22/09/2025    R DODD      1.0         Inserts rows of data into the AMENDMENTS table for the integration tests
*
**/

INSERT INTO amendments
(
amendment_id, business_unit_id ,associated_record_type ,associated_record_id
,amended_date, amended_by ,field_code
,old_value ,new_value
,case_reference ,function_code
)
VALUES
(
7 ,77 ,'defendant_accounts' ,'1'
,'2025-04-24 10:00:00' ,'User_A' ,1
,'Initial Data' ,'Updated Data'
,'Case_ref' ,'Func_code'
);

SELECT setval('amendment_id_seq', 70000000000000);
