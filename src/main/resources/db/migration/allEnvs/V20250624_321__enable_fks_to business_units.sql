/**
* OPAL Program
*
* MODULE      : enable_fks_to_business_units.sql
*
* DESCRIPTION : Re-enable foreign key constraints to the business_units table after data reload.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------------------
* 24/06/2025    C Cho       1.0         PO-1020 Re-enable foreign key constraints to business_units table after data reload.
*
**/

ALTER TABLE creditor_accounts ADD CONSTRAINT ca_business_unit_id_fk 
FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE courts ADD CONSTRAINT crt_business_unit_id_fk 
FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE enforcers ADD CONSTRAINT enf_business_unit_id_fk
FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE prisons ADD CONSTRAINT pri_business_unit_id_fk
FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE major_creditors ADD CONSTRAINT mc_business_unit_id_fk
FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE business_unit_users ADD CONSTRAINT buu_business_unit_id_fk
FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE configuration_items ADD CONSTRAINT ci_business_unit_id_fk
FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);
