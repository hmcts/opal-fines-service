/**
* OPAL Program
*
* MODULE      : create_v_interface_jobs_processed_file_summary.sql
*
* DESCRIPTION : Create view for processed Auto Payments In interface file summary
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 17/08/2026    C Cho          1.0         PO-2615 - Create v_interface_jobs_processed_file_summary view
*
**/

CREATE OR REPLACE VIEW v_interface_jobs_processed_file_summary AS
 SELECT inf.interface_file_id,
        inf.file_name AS interface_file_name,
        ij.interface_job_id,
        inf.source,
        bu.business_unit_id,
        bu.business_unit_name,
        inf.total_amount,
        inf.record_count AS total_records,
        COALESCE(im.total_errors, 0)::bigint AS total_errors,
        t.till_id,
        t.till_number
   FROM interface_jobs ij
   JOIN interface_files inf ON inf.interface_job_id = ij.interface_job_id
   LEFT JOIN tills t ON t.interface_file_id = inf.interface_file_id
   LEFT JOIN business_units bu ON bu.business_unit_id = t.business_unit_id
   LEFT JOIN (
        SELECT interface_messages.interface_file_id,
               COUNT(interface_messages.interface_message_id) AS total_errors
          FROM interface_messages
         WHERE interface_messages.message_type IN ('Exception', 'Warning')
         GROUP BY interface_messages.interface_file_id
   ) im ON im.interface_file_id = inf.interface_file_id
  WHERE ij.status = 'COMPLETED';

COMMENT ON VIEW v_interface_jobs_processed_file_summary IS 'Retrieves processed Auto Payments In interface file summary details';

