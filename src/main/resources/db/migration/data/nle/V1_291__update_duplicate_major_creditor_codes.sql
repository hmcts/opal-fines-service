/**
* OPAL Program
*
* MODULE      : update_duplicate_major_creditor_codes.sql
*
* DESCRIPTION : Update duplicate test major creditor codes before enforcing uniqueness per business unit.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 20/08/2026    C Cho       1.0         PO-10261 Update duplicate MAJOR_CREDITORS test data before adding the unique constraint.
*
**/

UPDATE major_creditors
   SET major_creditor_code = '0102',
       name = 'PO-9150 Major Creditor'
 WHERE major_creditor_id = 360000000102
   AND business_unit_id = 36
   AND major_creditor_code = '2';

UPDATE major_creditors
   SET major_creditor_code = '0142',
       name = 'PO-9150 Major Creditor'
 WHERE major_creditor_id = 360000000142
   AND business_unit_id = 36
   AND major_creditor_code = '2';

UPDATE major_creditors
   SET major_creditor_code = '0950',
       name = 'PO-9150 Major Creditor'
 WHERE major_creditor_id = 99000000000950
   AND business_unit_id = 77
   AND major_creditor_code = 'TEST';
