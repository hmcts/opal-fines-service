/**
*
* OPAL Program
*
* MODULE      : write_off_report_data.sql
*
* DESCRIPTION : Load REPORTS reference data for Admin Write Off
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 10/06/2026    P Brumby    1.0         PO-3455 Load REPORTS reference data for Admin Write Off
*
**/

INSERT INTO reports (report_id, report_title, report_group, audited_report, report_parameters, supports_multi_bu, is_bespoke_journey, shown_as_worklist, retention_period, permission, supported_file_types, can_manually_create) VALUES 
('write_off_report', 'Write off report', 'Auditing', false, NULL, false, false, true, 'P7D', NULL, NULL, true);
