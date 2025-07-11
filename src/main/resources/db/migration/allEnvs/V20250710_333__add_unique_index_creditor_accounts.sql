/**
* CGI OPAL Program
*
* MODULE      : add_unique_index_creditor_accounts.sql
*
* DESCRIPTION : Add unique index to CREDITOR_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------
* 09/07/2025    TMc         1.0         PO-899 Add index ca_acc_num_bu_udx to CREDITOR_ACCOUNTS table
*
**/
CREATE UNIQUE INDEX ca_acc_num_bu_udx ON creditor_accounts (account_number, business_unit_id);