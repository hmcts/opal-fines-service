/**
* OPAL Program
*
* MODULE      : column_version_number.sql
*
* DESCRIPTION : Add the VERSION_NUMBER to the DRAFT_ACCOUNTS table to help use JPA to investigate the most appropriate locking mechanism to adopt. NOTE this column will be dropped if no longer required.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 25/02/2025    A Dennis    1.0         PO-1297 Add the VERSION_NUMBER to the DRAFT_ACCOIUNTS table to help use JPA to investigate the most appropriate locking mechanism to adopt. NOTE this column will be dropped if no longer required.
*
**/
ALTER TABLE DRAFT_ACCOUNTS ADD version_number bigint;
COMMENT ON COLUMN draft_accounts.version_number IS 'To be used to hold versions of row data as they change to help in the locking mechanism if adopted.';
