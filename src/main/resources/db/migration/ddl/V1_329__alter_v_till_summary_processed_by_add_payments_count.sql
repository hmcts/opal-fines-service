/**
* OPAL Program
*
* MODULE      : alter_v_till_summary_processed_by_add_payments_count.sql
*
* DESCRIPTION : Update processed by and add payments count to the till summary view
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 19/08/2026    C Cho          1.0         PO-2594 - Create v_till_summary view
* 29/09/2026    C Cho          2.0         PO-10703 - Update processed by and add payments count
*
**/
DROP VIEW IF EXISTS v_till_summary;

CREATE VIEW v_till_summary AS
 SELECT t.till_id,
        t.till_number,
        COALESCE(im.errors, 0)::bigint AS errors,
        t.interface_file_id,
        inf.file_name,
        t.source,
        t.total_amount AS amount,
        t.business_unit_id,
        bu.business_unit_name,
        t.owned_by_name AS processed_by,
        t.created_date AS date_processed,
        t.auto_payment,
        t.status,
        t.payments_count
   FROM tills t
   JOIN business_units bu ON bu.business_unit_id = t.business_unit_id
   LEFT JOIN interface_files inf ON inf.interface_file_id = t.interface_file_id
   LEFT JOIN (
        SELECT interface_messages.interface_file_id,
               COUNT(*) AS errors
          FROM interface_messages
         WHERE interface_messages.message_type IN ('Exception', 'Warning')
         GROUP BY interface_messages.interface_file_id
   ) im ON im.interface_file_id = t.interface_file_id;

COMMENT ON VIEW v_till_summary IS 'Retrieves Auto Payments In till summary details';