/**
* OPAL Program
*
* MODULE      : Create_t_report_permissions_enum.sql
*
* DESCRIPTION : Creates the t_report_permissions_enum type to hold
*               report-specific permissions.
*
* PARAMETERS  : None
*
* VERSION HISTORY:
*
* Date         Author      Version   Nature of Change
* -----------  ----------  --------  ----------------------------------------
* 11/11/2026   I Chandio   1.0      PO-11469.Create an enum of permissions to be used by Reports
*
**/

CREATE TYPE t_report_permissions_enum AS ENUM
(
    'REVERSE_PAYMENT_REPORT'
);

COMMENT ON TYPE t_report_permissions_enum IS
    'Defines report-specific permissions used to control access to individual reports.';