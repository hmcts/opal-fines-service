/**
* OPAL Program
*
* MODULE      : column_version_number.sql
*
* DESCRIPTION : Add other 'lock' columns to the DRAFT_ACCOUNTS table to help investigate the most appropriate locking mechanism to adopt
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 26/02/2025    R Dodd      1.0         PO-1148 Add lock_id_data and lock_timeout columns to draft_accounts table
*
**/
ALTER TABLE DRAFT_ACCOUNTS ADD lock_id_data   varchar(100);
ALTER TABLE DRAFT_ACCOUNTS ADD lock_timeout   timestamp;
