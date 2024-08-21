/**
*
* OPAL Program
*
* MODULE      : modify_defendant_accounts.sql
*
* DESCRIPTION : Modified DEFENDANT_ACCOUNTS table to add originator_id column and drop originator_reference column.
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ----------------------------------------------------------------------------------------------------------
* 20/08/2024    A Dennis     1.0         PO-641 Modified DEFENDANT_ACCOUNTS table to add originator_id column and drop originator_reference column.
*
**/     

ALTER TABLE defendant_accounts
DROP COLUMN IF EXISTS originator_reference;

ALTER TABLE defendant_accounts
ADD COLUMN originator_id        varchar(40);

COMMENT ON COLUMN defendant_accounts.originator_id IS 'ID of the Originator';
