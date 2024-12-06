/**
* OPAL Program
*
* MODULE      : warrant_register.sql
*
* DESCRIPTION : Create the WARRANT_REGISTER table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-397 Create the WARRANT_REGISTER table in the Fines model
*
**/ 

CREATE TABLE warrant_register
(
 warrant_register_id       bigint
,business_unit_id          smallint         not null 
,enforcer_id               bigint           not null
,enforcement_id            bigint
,CONSTRAINT warrant_register_pk PRIMARY KEY (warrant_register_id)
,CONSTRAINT wr_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
);

COMMENT ON COLUMN warrant_register.warrant_register_id IS 'Unique ID of this record';
COMMENT ON COLUMN warrant_register.business_unit_id IS 'ID of the relating till to which this till belongs';
COMMENT ON COLUMN warrant_register.enforcer_id IS 'ID of the enforcer this warrant is allocated to';
COMMENT ON COLUMN warrant_register.enforcement_id IS 'ID of the enforcement action that generated this warrant';