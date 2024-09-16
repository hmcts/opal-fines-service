/**
* OPAL Program
*
* MODULE      : restore_for_business_units.sql
*
* DESCRIPTION : Enable the foreign key relationships that were disabled to allow loading of Business Units Reference Data from the Legacy GoB environment. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Enable the foreign key relationships that were disabled to allow loading of Business Units Reference Data from the Legacy GoB environment.  
*
**/

-- Firstly, remove any existing Opal child reference data, loaded from spreadsheets, that now do not exist in the source business units taken from the Oracle Legacy GoB system test environment
DELETE FROM courts WHERE business_unit_id IN (67, 107, 109, 110, 111, 113, 116);

-- Put back constraints
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

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_imposing_court_id_fk FOREIGN KEY
(
  imposing_court_id
)
REFERENCES courts
(
  court_id 
);

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_enforcing_court_id_fk FOREIGN KEY
(
  enforcing_court_id
)
REFERENCES courts
(
  court_id 
);

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_last_hearing_court_id_fk FOREIGN KEY
(
  last_hearing_court_id
)
REFERENCES courts
(
  court_id 
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

ALTER TABLE offences
ADD CONSTRAINT off_business_unit_id_fk FOREIGN KEY
(
  business_unit_id
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE major_creditors
ADD CONSTRAINT mc_business_unit_id_fk FOREIGN KEY
(
  business_unit_id
)
REFERENCES business_units
(
  business_unit_id 
);
