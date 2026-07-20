/**
* OPAL Program
*
* MODULE      : create_auto_payment_interface_indexes.sql
*
* DESCRIPTION : Add indexes required for Auto Payments In interface processing.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 30/06/2026    C Cho       1.0         PO-2616 Add Auto Payments In interface indexes.
*
**/

CREATE INDEX IF NOT EXISTS ij_status_created_idx
    ON interface_jobs(status, interface_name, created_datetime);

CREATE INDEX IF NOT EXISTS if_interface_job_id_idx
    ON interface_files(interface_job_id);

CREATE INDEX IF NOT EXISTS im_interface_job_id_idx
    ON interface_messages(interface_job_id);

CREATE INDEX IF NOT EXISTS im_interface_file_id_idx
    ON interface_messages(interface_file_id);

CREATE INDEX IF NOT EXISTS im_message_text_idx
    ON interface_messages(message_text);

CREATE UNIQUE INDEX IF NOT EXISTS ci_item_name_bu_idx
    ON configuration_items(item_name, business_unit_id);
