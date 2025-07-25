/**
* CGI OPAL Program
*
* MODULE      : amend_index_ma_acc_num_bu_udx.sql
*
* DESCRIPTION : Change the column order of index MA_ACC_NUM_BU_UDX and rename to MA_BU_ACC_NUM_UDX
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------
* 18/07/2025    TMc         1.0         PO-1958 Change the column order of index MA_ACC_NUM_BU_UDX and rename to MA_BU_ACC_NUM_UDX
*
**/
DROP INDEX IF EXISTS ma_acc_num_bu_udx;

CREATE UNIQUE INDEX ma_bu_acc_num_udx ON miscellaneous_accounts (business_unit_id, account_number);