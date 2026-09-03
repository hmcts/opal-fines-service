/**
* OPAL Program
*
* MODULE      : create_v_outstanding_auto_payment_count.sql
*
* DESCRIPTION : Create view for outstanding Auto Payments In counts by business unit
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 27/07/2026    C Cho          1.0         PO-2591 - Create v_outstanding_auto_payment_count view
*
**/

CREATE OR REPLACE VIEW v_outstanding_auto_payment_count AS
 SELECT bu.business_unit_id,
        bu.business_unit_name,
        COALESCE(ij.files_to_process_count, 0)::bigint AS files_to_process_count,
        COALESCE(t.tills_to_allocate_count, 0)::bigint AS tills_to_allocate_count
   FROM business_units bu
   LEFT JOIN (
        SELECT interface_jobs.business_unit_id,
               COUNT(*) AS files_to_process_count
          FROM interface_jobs
         WHERE interface_jobs.status IN ('CREATED', 'FAILED')
         GROUP BY interface_jobs.business_unit_id
   ) ij ON ij.business_unit_id = bu.business_unit_id
   LEFT JOIN (
        SELECT tills.business_unit_id,
               COUNT(*) AS tills_to_allocate_count
          FROM tills
         WHERE tills.status IN ('Created', 'Failed')
           AND tills.auto_payment IS TRUE
         GROUP BY tills.business_unit_id
   ) t ON t.business_unit_id = bu.business_unit_id;

COMMENT ON VIEW v_outstanding_auto_payment_count IS 'Retrieves outstanding Auto Payments In file and till counts by business unit';

