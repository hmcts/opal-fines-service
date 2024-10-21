/**
* OPAL Program
*
* MODULE      : aliases_surname_nullable.sql
*
* DESCRIPTION : Make the SURNAME column in ALIASES table nullable because there is now organisation_name.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 21/10/2024    A Dennis    1.0         PO-920  Make the SURNAME column in ALIASES table nullable because there is now organisation_name
*
**/

ALTER TABLE aliases
ALTER COLUMN surname DROP NOT NULL;
