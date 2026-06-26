/**
*
* OPAL Program
*
* MODULE      : write_off_codes_data.sql
*
* DESCRIPTION : Load WRITE_OFF_CODES reference data for Admin Write Off
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 09/06/2026    P Brumby    1.0         PO-3454 Load WRITE_OFF_CODES reference data for Admin Write Off
*
**/

-- Write-Off Code value 'JCAM-J' is not used and has no configuration values defined, so is not inserted.

INSERT INTO write_off_codes (
    write_off_code_id,
    reason_description,
    category,
    write_off_code_value
)
VALUES
    ('JCAM-A', 'Unknown Whereabouts', 'Admin', 1020),
    ('JCAM-B', 'Emigrated / Gone Abroad', 'Admin', 1030),
    ('JCAM-C', 'Deceased', 'Admin', 1040),
    ('JCAM-D', 'Sent to Mental Health Institution', 'Admin', 1050),
    ('JCAM-E', 'Sum Less Than £10', 'Admin', 1060),
    ('JCAM-F', 'Imprisonment 12 Months', 'Admin', 1070),
    ('JCAM-G', 'Limited Company Wound Up', 'Admin', 1080),
    ('JCAM-H', 'Serviceman - Military Correctional Training', 'Admin', 1090),
    ('JCAM-I', 'Local Authority Moved to Scotland', 'Admin', 1100),
    ('JCAM-K', 'Other', 'Admin', 1120),
    ('REMITT', 'Remitted', 'Judicial', 1130),
    ('IMPRIS', 'Satisfied by Imprisonment', 'Judicial', 1140),
    ('APPEAL', 'Appeals', 'Judicial', 1150),
    ('CTPROC', 'Statutory Declarations (Court Proceedings)', 'Judicial', 1160),
    ('FIXPEN', 'Statutory Declarations (Fixed Penalty)', 'Judicial', 1170),
    ('REVIEW', 'Compensation No Longer Payable (Criminal Courts Act)', 'Judicial', 1180),
    ('INPERR', 'Input Error', 'Judicial', 1190),
    ('OTHERS', 'Others (non-specified)', 'Judicial', 1200),
    ('AMTCON', 'Consolidated', 'System', 1000),
    ('TRNOUT', 'Transferred Out', 'System', 1010);
