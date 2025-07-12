/**
* CGI OPAL Program
*
* MODULE      : add_unique_index_defendant_accounts.sql
*
* DESCRIPTION : Add unique index to DEFENDANT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------
* 09/07/2025    TMc         1.0         PO-899 Add unique index da_acc_num_bu_udx to DEFENDANT_ACCOUNTS table
*
**/
CREATE UNIQUE INDEX da_acc_num_bu_udx ON defendant_accounts (account_number, business_unit_id);