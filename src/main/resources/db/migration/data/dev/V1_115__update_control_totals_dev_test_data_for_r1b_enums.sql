/**
* OPAL Program
*
* MODULE      : update_control_totals_dev_test_data_for_r1b_enums.sql
*
* DESCRIPTION : Update control_totals dev test data for R1b enum values
*
* VERSION HISTORY:
*
* Date          Author    Version     Nature of Change
* ----------    ------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc       1.0         PO-3622 - Update columns on CONTROL_TOTALS table to use PostgreSQL ENUM
*
**/

UPDATE control_totals
SET associated_record_type = 'report_instances'
WHERE associated_record_type = 'REPORT_INSTANCE';