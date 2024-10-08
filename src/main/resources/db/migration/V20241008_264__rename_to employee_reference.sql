/**
* OPAL Program
*
* MODULE      : rename_to_employee_reference.sql
*
* DESCRIPTION : Change the column name employer_reference to employee_reference in the DEBTOR_DETAIL table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------
* 08/10/2024    A Dennis    1.0         PO-806 Change the column name employer_reference to employee_reference in the DEBTOR_DETAIL table.
*
**/

ALTER TABLE debtor_detail
RENAME COLUMN employer_reference TO employee_reference;

COMMENT ON COLUMN debtor_detail.employee_reference IS 'Employee reference number';
