/**
* CGI OPAL Program
*
* MODULE      : configuration_items.sql
*
* DESCRIPTION : Creates the CONFIGURATION_ITEMS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 20/03/2024    A Dennis    1.0         PO-227 Creates the CONFIGURATION_ITEMS table for the Fines model
*
**/
CREATE TABLE configuration_items 
(
 configuration_item_id     bigint         not null
,item_name                 varchar(50)    not null
,business_unit_id          smallint
,item_value                text           
,item_values               varchar(500)[]
,CONSTRAINT configuration_items_pk PRIMARY KEY 
 (
   configuration_item_id	
 ) 
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

CREATE INDEX IF NOT EXISTS ci_item_name_idx
ON configuration_items(item_name);

COMMENT ON COLUMN configuration_items.configuration_item_id IS 'Configuration item ID';
COMMENT ON COLUMN configuration_items.item_name IS 'Configuration item name';
COMMENT ON COLUMN configuration_items.business_unit_id IS 'ID of the business unit or NULL for all';
COMMENT ON COLUMN configuration_items.item_value IS 'Single text value';
COMMENT ON COLUMN configuration_items.item_values IS 'Multiple values';
