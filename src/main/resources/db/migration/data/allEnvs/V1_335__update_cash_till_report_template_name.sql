/**
* OPAL Program
*
* MODULE      : update_cash_till_report_template_name.sql
*
* DESCRIPTION : Populate the Cash Till report template name.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 08/10/2026    TMc            1.0         PO-10588 - Populate the Cash Till report template name.
*
**/

UPDATE reports
   SET template_name = 'CR-OPL-GRS-ENG-CASH-TILL-REPORT-v1.docx'
 WHERE report_id = 'cash_till';

