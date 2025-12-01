/**
* CGI OPAL Program
*
* MODULE      : insert_into_results.sql
*
* DESCRIPTION : Load the RESULTS table with reference data for the Integration Tests
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 21/05/2025    R DODD      1.0         Inserts rows of data into the RESULTS table for the integration tests
*
**/

INSERT INTO results
(
result_id,result_title,result_title_cy,result_type
,active,imposition,imposition_category,imposition_allocation_priority,imposition_accruing,imposition_creditor
,enforcement,enforcement_override,further_enforcement_warn,further_enforcement_disallow,enforcement_hold
,requires_enforcer,generates_hearing,generates_warrant,collection_order
,extend_ttp_disallow,extend_ttp_preserve_last_enf,prevent_payment_card,lists_monies,result_parameters
)
VALUES
(
'AAAAAA','First Ever Result Entry for Testing','Cais am dynnu arian o fudd-daliadau','Action'
,TRUE,FALSE,NULL,NULL,FALSE,NULL
,TRUE,TRUE,TRUE,FALSE,FALSE
,TRUE,FALSE,FALSE,FALSE
,FALSE,FALSE,FALSE,FALSE,NULL
),
(
'BBBBBB','Complete Result Entry for Testing','Cais Prawf Cyflawn','Action'
,TRUE,TRUE,'Compensation',5,TRUE,'CF'
,TRUE,TRUE,TRUE,TRUE,TRUE
,TRUE,TRUE,TRUE,TRUE
,TRUE,TRUE,TRUE,TRUE,'{"param1":"value1","param2":"value2"}'
);
