/**
* OPAL Program
*
* MODULE      : backfill_cash_till_report_instance_template_name.sql
*
* DESCRIPTION : Backfill the template name on development Cash Till report instances.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 08/10/2026    TMc            1.0         PO-10589 - Backfill development Cash Till report instance template names.
*
**/

UPDATE report_instances AS ri
   SET template_name = r.template_name
  FROM reports AS r
 WHERE ri.report_id = r.report_id
   AND r.report_id = 'cash_till'
   AND r.template_name IS NOT NULL;
