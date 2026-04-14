/**
* CGI OPAL Program
*
* MODULE      : amend_dev_test_data_for_r1a_enums.sql
*
* DESCRIPTION : Amend dev test data to align with values in the new ENUM data types
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -----------------------------------------------------------------------------------------------------------------
* 05/03/2026    T McCallion    1.0         PO-2868 - Update columns on DEFENDANT_ACCOUNTS table to use postgresql enum instead of varchar
*                                             DEFENDANT_ACCOUNTS.ACCOUNT_STATUS. Update records where the value (C) is invalid, set it to a valid value (CS)
*                                             DEFENDANT_ACCOUNTS.ORIGINATOR_TYPE. Update records where the values are invalid.
*                                          PO-2910 - Update columns on PAYMENT_TERMS table to use postgresql enum instead of varchar
*                                             PAYMENT_TERMS.INSTALMENT_PERIOD. Update record where the value (Q) is invalid, set it to a valid value (W)
*                                          PO-2930 - Update columns on DOCUMENT_INSTANCES table to use postgresql enum instead of varchar
*                                             DOCUMENT_INSTANCES.STATUS. Update records where the value (Generated) is invalid, set it to a valid value (New)
**/

--DEFENDANT_ACCOUNTS - PO-2868
UPDATE defendant_accounts
   SET account_status = 'CS'
 WHERE account_status = 'C';
 
UPDATE defendant_accounts
   SET originator_type = 'NEW'
 WHERE originator_type = 'MANUAL';
 
UPDATE defendant_accounts
   SET originator_type = 'FP'
 WHERE originator_type = 'AUTO_FP';
 
UPDATE defendant_accounts
   SET originator_type = 'TFO'
 WHERE originator_type = 'AUTO_CP';
 

--PAYMENT_TERMS - PO-2910
UPDATE payment_terms 
   SET instalment_period = 'W'
 WHERE instalment_period = 'Q';
 

--DOCUMENT_INSTANCES - PO-2930
UPDATE document_instances
   SET status = 'New'
 WHERE status = 'Generated';