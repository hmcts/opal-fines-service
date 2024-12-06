/**
* OPAL Program
*
* MODULE      : add_org_name_to_aliases.sql
*
* DESCRIPTION : Add organisation_name column to the ALIASES table so we don't use surname column for organisations. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------
* 04/07/2024    A Dennis    1.0         PO-445 Add organisation_name column to the ALIASES table so we don't use surname column for organisations. 
*
**/
ALTER TABLE aliases
ADD COLUMN organisation_name        varchar(50);

COMMENT ON COLUMN aliases.organisation_name IS 'Alias organisation name';
