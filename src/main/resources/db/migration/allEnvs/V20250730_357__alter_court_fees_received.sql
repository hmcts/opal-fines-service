/**
* CGI OPAL Program
*
* MODULE      : alter_court_fees_received.sql
*
* DESCRIPTION : Add column RECEIVED_DATE to the COURT_FEES_RECEIVED table and make SUSPENSE_TRANSACTION_ID nullable
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 30/07/2025    TMc         1.0         PO-1102 - Add column RECEIVED_DATE to the COURT_FEES_RECEIVED table and make SUSPENSE_TRANSACTION_ID nullable
*
**/
ALTER TABLE court_fees_received  
    ADD COLUMN received_date TIMESTAMP,
    ALTER COLUMN suspense_transaction_id DROP NOT NULL;

COMMENT ON COLUMN court_fees_received.received_date IS 'Date the court fee was received';
