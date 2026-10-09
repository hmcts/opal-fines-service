/**
* OPAL Program
*
* MODULE      : alter_report_instances_add_template_name.sql
*
* DESCRIPTION : Add the point-in-time template name to REPORT_INSTANCES.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 08/10/2026    TMc            1.0         PO-10589 - Add the template name to REPORT_INSTANCES.
*
**/

ALTER TABLE report_instances
    ADD COLUMN template_name VARCHAR(100);

COMMENT ON COLUMN report_instances.template_name IS 'The name of the template for the associated report at the point in time the report instance was created.';

