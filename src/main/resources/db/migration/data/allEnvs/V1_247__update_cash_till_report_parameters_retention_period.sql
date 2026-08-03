/**
* OPAL Program
*
* MODULE      : update_cash_till_report_parameters_retention_period.sql
*
* DESCRIPTION : Update the cash till report parameters and retention period.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 30/07/2026    C Cho       1.0         PO-9061 Update CASH_TILL report parameters and retention period.
*
**/

UPDATE reports
   SET report_parameters = '[{"name":"till_id","prompt":"Till ID","type":"integer","mandatory":true,"min":1},{"name":"allocated_report","prompt":"Allocated report","type":"boolean","mandatory":false}]'
      ,retention_period = 'P14D'
 WHERE report_id = 'cash_till';