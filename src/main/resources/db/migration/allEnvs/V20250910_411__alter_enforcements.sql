/**
* CGI OPAL Program
*
* MODULE      : alter_enforcements.sql
*
* DESCRIPTION : Add column EARLIEST_RELEASE_DATE to the ENFORCEMENTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------------------------------------------------------------
* 05/09/2025    C Cho       1.0         PO-1971 Add column EARLIEST_RELEASE_DATE to the ENFORCEMENTS table
*
**/
ALTER TABLE enforcements 
    ADD COLUMN earliest_release_date TIMESTAMP;

COMMENT ON COLUMN enforcements.earliest_release_date IS 'The earliest release date for a PRIS enforcement action';