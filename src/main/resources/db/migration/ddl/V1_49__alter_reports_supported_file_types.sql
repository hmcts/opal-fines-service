/**
* OPAL Program
*
* MODULE      : alter_reports_supported_file_types.sql
*
* DESCRIPTION : Add JSON to the REPORTS.SUPPORTED_FILE_TYPES enum values.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 15/05/2026    C Cho       1.0         PO-3955 Add JSON to the REPORTS.SUPPORTED_FILE_TYPES enum values.
*
**/

ALTER TYPE r_supported_file_type_enum ADD VALUE 'JSON';

COMMENT ON COLUMN reports.supported_file_types IS 'An enum array supporting the following file types: CSV, PDF, XML, JSON.';
