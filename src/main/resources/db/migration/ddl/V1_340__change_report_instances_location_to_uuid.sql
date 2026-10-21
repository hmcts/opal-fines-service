/**
* OPAL Program
*
* MODULE      : change_report_instances_location_to_uuid.sql
*
* DESCRIPTION : Converts REPORT_INSTANCES.LOCATION from VARCHAR(50) to UUID
*               to align the database schema with the application entity
*               mapping (ReportInstanceEntity.location).
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------
* 21/10/2026    CW          1.0         Change REPORT_INSTANCES.LOCATION to UUID.
*
**/

ALTER TABLE report_instances
ALTER COLUMN location TYPE UUID
    USING location::uuid;

COMMENT ON COLUMN report_instances.location IS
    'The Azure unique identifier of the stored report location.';