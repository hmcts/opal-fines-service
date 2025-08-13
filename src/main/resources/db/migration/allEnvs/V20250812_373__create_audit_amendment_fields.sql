/**
* CGI OPAL Program
*
* MODULE      : create_audit_amendment_fields.sql
*
* DESCRIPTION : Create the AUDIT_AMENDMENT_FIELDS table in the OPAL Fines database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------
* 12/08/2025    C Cho       1.0         PO-1646 Create the AUDIT_AMENDMENT_FIELDS table in the OPAL Fines database.
*
**/
DROP TABLE IF EXISTS audit_amendment_fields;

CREATE TABLE audit_amendment_fields
(
    field_code  SMALLINT     NOT NULL
   ,data_item   VARCHAR(50)  NOT NULL
   ,CONSTRAINT audit_amendment_fields_pk PRIMARY KEY (field_code)
);

COMMENT ON COLUMN audit_amendment_fields.field_code IS 'The numeric value assigned to the field';
COMMENT ON COLUMN audit_amendment_fields.data_item  IS 'The name of the data held against the field code';