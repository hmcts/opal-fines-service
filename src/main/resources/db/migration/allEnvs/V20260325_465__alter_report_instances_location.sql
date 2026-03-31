/**
* OPAL Program
*
* MODULE      : alter_report_instances_location.sql
*
* DESCRIPTION : Amend REPORT_INSTANCES.LOCATION for enforcement operational reporting.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 26/03/2026    C Cho       1.0         PO-3413 Amend REPORT_INSTANCES.LOCATION to allow NULLs and increase length.
*
**/

ALTER TABLE report_instances
    ALTER COLUMN location DROP NOT NULL;

ALTER TABLE report_instances
    ALTER COLUMN location TYPE varchar(50);
