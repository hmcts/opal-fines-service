/**
* CGI OPAL Program
*
* MODULE      : add_unique_index_miscellaneous_accounts.sql
*
* DESCRIPTION : Add unique index to MISCELLANEOUS_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------
* 09/07/2025    TMc         1.0         PO-899 Add index ma_acc_num_bu_udx to MISCELLANEOUS_ACCOUNTS table
*
**/
CREATE UNIQUE INDEX ma_acc_num_bu_udx ON miscellaneous_accounts (account_number, business_unit_id);