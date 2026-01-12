/**
* OPAL Program
*
* MODULE      : alter_reports_for_enforcement_reporting.sql
*
* DESCRIPTION : Add reporting flags and metadata to REPORTS table for enforcement operational reporting.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------
* 23/12/2025    C Cho       1.0         PO-2269 Add reporting flags and metadata to REPORTS table for enforcement operational reporting.
**/
CREATE TYPE r_supported_file_type_enum AS ENUM ('CSV', 'PDF', 'XML');

ALTER TABLE reports
    ADD COLUMN supports_multi_bu BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN is_bespoke_journey BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN shown_as_worklist BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN retention_period VARCHAR(30),
    ADD COLUMN permission VARCHAR(30),
    ADD COLUMN supported_file_types r_supported_file_type_enum[],
    ADD COLUMN can_manually_create BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE reports
    ALTER COLUMN supports_multi_bu DROP DEFAULT,
    ALTER COLUMN is_bespoke_journey DROP DEFAULT,
    ALTER COLUMN shown_as_worklist DROP DEFAULT,
    ALTER COLUMN can_manually_create DROP DEFAULT;

COMMENT ON COLUMN reports.report_parameters IS
    'One of: A json array containing the generic parameters the report uses. '
    'Null if the report does not support generic parameters and must instead rely on a bespoke implementation. '
    'Usually when the report params are complex so they can not be represented in a generic fashion (e.g list fines)';
COMMENT ON COLUMN reports.supports_multi_bu IS 'Whether the report can be run across multiple business units.';
COMMENT ON COLUMN reports.is_bespoke_journey IS 'Whether the report follows the standard journey, or needs bespoke screens (e.g. warrant register).';
COMMENT ON COLUMN reports.shown_as_worklist IS 'Whether the report is shown as a worklist.';
COMMENT ON COLUMN reports.retention_period IS 'An ISO 8601 duration indicating how long after creation of a report instance it should be deleted.';
COMMENT ON COLUMN reports.permission IS 'The permission the user must have in order to use this report.';
COMMENT ON COLUMN reports.supported_file_types IS 'An enum array supporting the following file types: CSV, PDF, XML.';
COMMENT ON COLUMN reports.can_manually_create IS 'Whether the user can manually create this report.';
