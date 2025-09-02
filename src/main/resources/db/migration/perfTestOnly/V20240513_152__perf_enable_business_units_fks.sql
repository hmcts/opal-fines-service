/**
* OPAL Program
*
* MODULE      : cleanup_business_units.sql
*
* DESCRIPTION : Put back foreign keys to the BUSINESS_UNITS table after loading Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------
* 13/05/2024    A Dennis    1.0         PO-306 Put back foreign keys to the BUSINESS_UNITS table after loading Reference Data from data held in Excel spreadsheet.  
*
**/

ALTER TABLE business_unit_users
ADD CONSTRAINT buu_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE configuration_items
ADD CONSTRAINT ci_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE courts
ADD CONSTRAINT crt_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE document_instances
ADD CONSTRAINT di_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE enforcers
ADD CONSTRAINT enf_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE prisons
ADD CONSTRAINT pri_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE tills
ADD CONSTRAINT till_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);
