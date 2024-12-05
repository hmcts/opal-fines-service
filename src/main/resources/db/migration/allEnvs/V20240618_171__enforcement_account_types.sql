/**
* OPAL Program
*
* MODULE      : enforcement_account_types.sql
*
* DESCRIPTION : Create the ENFORCEMENT_ACCOUNT_TYPES table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 18/06/2024    I Readman    1.0         PO-390 Create the ENFORCEMENT_ACCOUNT_TYPES table in the Fines model
*
**/ 

CREATE TABLE enforcement_account_types
(
 enforcement_account_type_id   bigint        not null
,business_unit_id              smallint      not null
,account_type                  varchar(20)   not null
,account_type_name             varchar(50)   not null
,minimum_balance               bigint 
,CONSTRAINT enforcement_account_types_pk PRIMARY KEY(enforcement_account_type_id)
,CONSTRAINT eat_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id) 
); 

COMMENT ON COLUMN enforcement_account_types.enforcement_account_type_id IS 'Unique ID of this record';
COMMENT ON COLUMN enforcement_account_types.business_unit_id IS 'ID of the business unit this account type belongs to';
COMMENT ON COLUMN enforcement_account_types.account_type IS 'The type of account this enforcement type is for, e.g. Fixed Penalties';
COMMENT ON COLUMN enforcement_account_types.account_type_name IS 'Name for this account, e.g. High Value Fixed Penalties';
COMMENT ON COLUMN enforcement_account_types.minimum_balance IS 'The minimum balance an account must have to be in this type';