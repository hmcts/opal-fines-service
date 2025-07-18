/**
* CGI OPAL Program
*
* MODULE      : add_index_creditor_accounts.sql
*
* DESCRIPTION : Add index to the CREDITOR_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1586 Add index ca_mcpid_idx to the CREDITOR_ACCOUNTS table
*
**/
CREATE INDEX ca_mcpid_idx ON creditor_accounts (minor_creditor_party_id);