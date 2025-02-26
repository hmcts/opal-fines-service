/**
* OPAL Program
*
* MODULE      : add_validated_by_name_column.sql
*
* DESCRIPTION : Add new column validated_by_name to the DRAFT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------
* 13/01/2025    A Dennis    1.0         PO-1028 Add new column validated_by_name to the DRAFT_ACCOUNTS table
*
**/

ALTER TABLE draft_accounts 
ADD COLUMN validated_by_name varchar(100);

COMMENT ON COLUMN draft_accounts.validated_by_name IS 'Name value of the validating user from the AAD Access Token';
