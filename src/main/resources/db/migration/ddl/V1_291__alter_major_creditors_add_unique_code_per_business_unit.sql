/**
* OPAL Program
*
* MODULE      : alter_major_creditors_add_unique_code_per_business_unit.sql
*
* DESCRIPTION : Add unique constraint to ensure MAJOR_CREDITORS.MAJOR_CREDITOR_CODE is unique per business unit.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 20/08/2026    C Cho       1.0         PO-10261 Add unique constraint on MAJOR_CREDITORS.BUSINESS_UNIT_ID and MAJOR_CREDITORS.MAJOR_CREDITOR_CODE.
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

ALTER TABLE major_creditors
    ADD CONSTRAINT mc_business_unit_id_major_creditor_code_uk UNIQUE (business_unit_id, major_creditor_code);
