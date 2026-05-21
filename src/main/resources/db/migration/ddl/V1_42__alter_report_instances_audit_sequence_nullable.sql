/**
* OPAL Program
*
* MODULE      : alter_report_instances_audit_sequence_nullable.sql
*
* DESCRIPTION : Amend REPORT_INSTANCES.AUDIT_SEQUENCE to allow NULL values.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 12/05/2026    C Cho       1.0         PO-3893 Amend REPORT_INSTANCES.AUDIT_SEQUENCE to allow NULL values.
*
**/

ALTER TABLE report_instances
    ALTER COLUMN audit_sequence DROP NOT NULL;