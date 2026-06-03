/**
* OPAL Program
*
* MODULE      : alter_amendments_add_amended_by_name.sql
*
* DESCRIPTION : Add AMENDMENTS.AMENDED_BY_NAME to store the user name responsible for the event.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 22/05/2026    C Cho       1.0         PO-2595 Add AMENDMENTS.AMENDED_BY_NAME to support View Defendant Account History.
*
**/

ALTER TABLE amendments
    ADD COLUMN amended_by_name VARCHAR(100);

COMMENT ON COLUMN amendments.amended_by_name IS 'User name of the user responsible for the event';
