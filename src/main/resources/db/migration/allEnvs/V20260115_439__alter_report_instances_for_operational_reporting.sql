/**
* OPAL Program
*
* MODULE      : alter_report_instances_for_operational_reporting.sql
*
* DESCRIPTION : Amend REPORT_INSTANCES for enforcement operational reporting.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 09/01/2026    C Cho       1.0         PO-2273 Amend REPORT_INSTANCES for enforcement operational reporting.
*
**/

ALTER TABLE report_instances
    RENAME COLUMN generated_date TO created_timestamp;

ALTER TABLE report_instances
    ALTER COLUMN created_timestamp DROP NOT NULL;

ALTER TABLE report_instances
    RENAME COLUMN generated_by TO requested_by;

ALTER TABLE report_instances
    ALTER COLUMN requested_by TYPE bigint
    USING requested_by::bigint;

ALTER TABLE report_instances
    DROP CONSTRAINT IF EXISTS ri_business_unit_id_fk;

ALTER TABLE report_instances
    ALTER COLUMN business_unit_id DROP NOT NULL;

ALTER TABLE report_instances
    ALTER COLUMN business_unit_id TYPE smallint[]
    USING ARRAY[business_unit_id]::smallint[];

ALTER TABLE report_instances
    RENAME COLUMN content TO location;

ALTER TABLE report_instances
    ALTER COLUMN location TYPE varchar(30)
    USING location::varchar(30);

CREATE TYPE ri_generation_status_enum AS ENUM ('REQUESTED', 'IN_PROGRESS', 'READY', 'ERROR');

ALTER TABLE report_instances
    ADD COLUMN requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN generation_status ri_generation_status_enum NOT NULL,
    ADD COLUMN scheduled_deletion_timestamp TIMESTAMP,
    ADD COLUMN report_name VARCHAR(250),
    ADD COLUMN no_of_records SMALLINT,
    ADD COLUMN errors JSON,
    ADD COLUMN requested_by_name VARCHAR(100) NOT NULL;

COMMENT ON COLUMN report_instances.created_timestamp IS 'The timestamp the report instance was created.';
COMMENT ON COLUMN report_instances.requested_by IS 'ID of the user that requested this report instance.';
COMMENT ON COLUMN report_instances.business_unit_id IS 'An array of business unit ids this report instance was generated for.';
COMMENT ON COLUMN report_instances.location IS 'The location the report data is stored at. Used if reports are stored outside of the database such as in a blob store.';
COMMENT ON COLUMN report_instances.requested_at IS 'Indicates when the report was requested at. Default to the current timestamp.';
COMMENT ON COLUMN report_instances.generation_status IS 'Value can be one of: REQUESTED - The report has been requested but has not yet started generation. IN_PROGRESS - The report is currently generating. READY - The report has generated successfully and is ready to be viewed. ERROR - The report has failed to generate errors can be seen in the error json field.';
COMMENT ON COLUMN report_instances.scheduled_deletion_timestamp IS 'Calculated using reports.retention_period when report is created.';
COMMENT ON COLUMN report_instances.report_name IS 'Introduced to cater for situations where a report could have subtypes (e.g. warrant register), or where a report instance has a name that has additional information (e.g. an enforcement operational report). Otherwise this will be the same as reports. report_title';
COMMENT ON COLUMN report_instances.no_of_records IS 'The number of records in the report. Used to show the No. of records value on the report summary screen.';
COMMENT ON COLUMN report_instances.errors IS 'A list of errors that occurred when generating the report.';
COMMENT ON COLUMN report_instances.requested_by_name IS 'The name of the user who requested the report.';

CREATE INDEX ri_report_id_bu_id_request_at_idx ON report_instances (report_id, business_unit_id, requested_at);
CREATE INDEX ri_requested_by_bu_id_request_at_idx ON report_instances (requested_by, business_unit_id, requested_at);
CREATE INDEX ri_scheduled_deletion_timestamp_idx ON report_instances (scheduled_deletion_timestamp);
