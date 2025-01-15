/**
* OPAL Program
*
* MODULE      : modify_report_tables.sql
*
* DESCRIPTION : Changes to the REPORTS, REPORT_INSTANCE and REPORT_ENTRIES tables after reference data work done by Capita
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 14/01/2025    A Dennis    1.0         PO-970 Changes to the REPORTS, REPORT_INSTANCE and REPORT_ENTRIES tables after reference data work done by Capita
*
**/

ALTER TABLE reports
    ALTER COLUMN report_id TYPE varchar(30),
    DROP COLUMN IF EXISTS user_entries,
    DROP COLUMN IF EXISTS report_parameters,
    ADD COLUMN report_parameters json;

ALTER TABLE report_instances
    ALTER COLUMN report_id TYPE varchar(30),
    ALTER COLUMN report_parameters DROP NOT NULL,
    ADD CONSTRAINT ri_report_id_fk
        FOREIGN KEY (report_id)
        REFERENCES reports (report_id);

ALTER TABLE report_entries
    ALTER COLUMN report_id TYPE varchar(30),
    DROP COLUMN IF EXISTS report_instances_id,
    ADD COLUMN report_instance_id bigint,
    ADD CONSTRAINT re_report_id_fk
        FOREIGN KEY (report_id)
        REFERENCES reports (report_id),
    ADD CONSTRAINT re_report_instance_id_fk
        FOREIGN KEY (report_instance_id)
        REFERENCES report_instances (report_instance_id)
        DEFERRABLE INITIALLY DEFERRED;

