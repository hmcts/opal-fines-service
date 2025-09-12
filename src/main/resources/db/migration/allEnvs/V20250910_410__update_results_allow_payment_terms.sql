/**
* CGI OPAL Program
*
* MODULE      : update_results_allow_payment_terms.sql
*
* DESCRIPTION : Update allow_payment_terms to TRUE for COLLO only 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 04/09/2025    C Cho       1.0         PO-1933 Update allow_payment_terms so only COLLO is true
*
**/

-- Update allow_payment_terms to FALSE for result ids that should not allow payment terms
UPDATE results 
SET allow_payment_terms = FALSE 
WHERE result_id IN ('ABDC', 'AEO', 'AEOC', 'REGF', 'S18', 'SC', 'UPWO');

-- Ensure COLLO has allow_payment_terms set to TRUE
UPDATE results 
SET allow_payment_terms = TRUE 
WHERE result_id = 'COLLO';