/**
* OPAL Program
*
* MODULE      : insert_track_enforcement_hold_report.sql
*
* DESCRIPTION : Insert the report definition used when tracking removed enforcement holds.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------
* 28/03/2026    Codex       1.0         PO-1775 Insert track enforcement hold report reference data.
**/

INSERT INTO reports (
    report_id,
    report_title,
    report_group,
    report_parameters,
    audited_report,
    supports_multi_bu,
    is_bespoke_journey,
    shown_as_worklist,
    retention_period,
    permission,
    supported_file_types,
    can_manually_create
)
SELECT
    'track_enforcement_hold',
    'Track Enforcement Hold',
    'Account Management',
    NULL,
    TRUE,
    FALSE,
    FALSE,
    FALSE,
    NULL,
    NULL,
    NULL,
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM reports
    WHERE report_id = 'track_enforcement_hold'
);
