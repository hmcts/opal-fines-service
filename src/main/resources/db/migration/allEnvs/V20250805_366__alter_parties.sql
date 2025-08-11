/**
* CGI OPAL Program
*
* MODULE      : alter_parties.sql
*
* DESCRIPTION : Drop column INITIALS from the PARTIES table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------
* 25/07/2025    TMc         1.0         PO-1977 - Drop column INITIALS from the PARTIES table.
*
**/
ALTER TABLE parties
    DROP COLUMN IF EXISTS initials;