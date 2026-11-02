/**
* OPAL Program
*
* MODULE      : alter_reports_add_template_name.sql
*
* DESCRIPTION : Add the template name to REPORTS.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 08/10/2026    TMc            1.0         PO-10588 - Add the template name to REPORTS.
*
**/

ALTER TABLE reports
    ADD COLUMN template_name VARCHAR(100);

COMMENT ON COLUMN reports.template_name IS 'The name of the template for the associated report.';

