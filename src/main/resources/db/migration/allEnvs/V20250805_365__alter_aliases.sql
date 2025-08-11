/**
* CGI OPAL Program
*
* MODULE      : alter_aliases.sql
*
* DESCRIPTION : Drop column INITIALS from the ALIASES table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------
* 25/07/2025    TMc         1.0         PO-1976 - Drop column INITIALS from the ALIASES table.
*
**/
ALTER TABLE aliases
    DROP COLUMN IF EXISTS initials;