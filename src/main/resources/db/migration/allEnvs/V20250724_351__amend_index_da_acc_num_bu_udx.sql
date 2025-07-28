/**
* CGI OPAL Program
*
* MODULE      : amend_index_da_acc_num_bu_udx.sql
*
* DESCRIPTION : Change the column order of index DA_ACC_NUM_BU_UDX and rename to DA_BU_ACC_NUM_UDX
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------------------------------
* 18/07/2025    TMc         1.0         PO-1958 Change the column order of index DA_ACC_NUM_BU_UDX and rename to DA_BU_ACC_NUM_UDX
*
**/
DROP INDEX IF EXISTS da_acc_num_bu_udx;

CREATE UNIQUE INDEX da_bu_acc_num_udx ON defendant_accounts (business_unit_id, account_number);