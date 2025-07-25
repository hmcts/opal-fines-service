/**
* CGI OPAL Program
*
* MODULE      : alter_account_number_index.sql
*
* DESCRIPTION : Rename the ACCOUNT_NUMBER_INDEX.ACCOUNT_INDEX_TYPE column to ASSOCIATED_RECORD_TYPE
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------
* 18/07/2025    TMc         1.0         PO-1959 Rename the ACCOUNT_NUMBER_INDEX.ACCOUNT_INDEX_TYPE column to ASSOCIATED_RECORD_TYPE 
*
**/
ALTER TABLE account_number_index
  RENAME COLUMN account_index_type TO associated_record_type;
