/**
* OPAL Program
*
* MODULE      : update_amendments_table_and_add_fk.sql
*
* DESCRIPTION : Update AMENDMENTS table to change old_value and new_value columns from varchar(200) to text, and add foreign key constraint for field_code column.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 19/08/2025    C Cho       1.0         PO-1651  Change old_value and new_value columns from varchar(200) to text, and add foreign key constraint for field_code to AUDIT_AMENDMENT_FIELDS.FIELD_CODE.
*
**/

ALTER TABLE amendments
ALTER COLUMN old_value TYPE text;

ALTER TABLE amendments
ALTER COLUMN new_value TYPE text;

ALTER TABLE amendments
ADD CONSTRAINT amend_field_code_fk FOREIGN KEY
(
  field_code
)
REFERENCES audit_amendment_fields
(
  field_code
);