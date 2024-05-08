/**
* CGI OPAL Program
*
* MODULE      : enable_tables_lja_fks.sql
*
* DESCRIPTION : Enable local justice areas foreign keys in tables that had them dropped in order to load LJA reference data for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------------------
* 05/05/2024    A Dennis    1.0         PO-305 Enable local justice areas foreign keys in tables that had them dropped in order to load LJA reference data for the Fines model
*
**/ 

ALTER TABLE account_transfers
ADD CONSTRAINT at_destination_lja_id_fk FOREIGN KEY
(
  destination_lja_id 
)
REFERENCES local_justice_areas
(
  local_justice_area_id 
);

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_enf_override_tfo_lja_id_fk FOREIGN KEY
(
  enf_override_tfo_lja_id 
)
REFERENCES local_justice_areas
(
  local_justice_area_id 
);
