/**
* CGI OPAL Program
*
* MODULE      : add_index_creditor_transactions.sql
*
* DESCRIPTION : Add index to the CREDITOR_TRANSACTIONS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1586 Add index ct_caid_tt_pp_idx to the CREDITOR_TRANSACTIONS table
*
**/
CREATE INDEX ct_caid_tt_pp_idx ON creditor_transactions (creditor_account_id, transaction_type, payment_processed);