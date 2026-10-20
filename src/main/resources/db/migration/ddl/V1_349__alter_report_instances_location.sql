/**
* OPAL Program
*
* MODULE      : alter_report_instances_location.sql
*
* DESCRIPTION : Alter location column to UUID on REPORT_INSTANCES.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 12/10/2026    TMc            1.0         PO-10589 - Alter report_instances table to add template name
*
**/

ALTER TABLE report_instances
    ALTER COLUMN location TYPE uuid
    USING location::uuid;

COMMENT ON COLUMN report_instances.location IS 'The Azure unique identifier of the stored report instance content JSON file.';

