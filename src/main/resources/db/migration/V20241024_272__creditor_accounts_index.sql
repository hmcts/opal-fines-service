/**
* OPAL Program
*
* MODULE      : creditor_account_index.sql
*
* DESCRIPTION : Finding the creditor for impositions does not use the primary key so create an index on CREDITOR_ACCOUNTS (business_unit_id, account_type).
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------
* 24/10/2024    A Dennis    1.0         PO-902  Finding the creditor for impositions does not use the primary key so create an index on CREDITOR_ACCOUNTS (business_unit_id, account_type)
**/

CREATE INDEX IF NOT EXISTS ca_bus_unit_acc_type_idx ON creditor_accounts(business_unit_id,creditor_account_type);
