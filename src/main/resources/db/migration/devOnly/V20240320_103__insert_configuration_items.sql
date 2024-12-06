/**
* OPAL Program
*
* MODULE      : insert_configuration_items.sql
*
* DESCRIPTION : Inserts rows of data into the CONFIGURATION_ITEMS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 20/03/2024    A Dennis    1.0         PO-227 Inserts rows of data into the CONFIGURATION_ITEMS table
*
**/
INSERT INTO configuration_items
(
 configuration_item_id	         
,item_name                 
,business_unit_id          
,item_value                          
,item_values                     
)
VALUES
(
 500000000
,'AUDIT_LOG_RETENTION_PERIOD_DAYS'
,null
,'100'
,NULL
);

INSERT INTO configuration_items
(
 configuration_item_id	         
,item_name                 
,business_unit_id          
,item_value                          
,item_values                     
)
VALUES
(
 500000001
,'COURT_TYPES'
,null
,null
,ARRAY['Magistrate', 'Crown', 'Appeal', 'High','Supreme']
);

INSERT INTO configuration_items
(
 configuration_item_id	         
,item_name                 
,business_unit_id          
,item_value                          
,item_values                     
)
VALUES
(
 500000002
,'RATE_TYPES'
,73
,null
,'{"Business", "Local", "County", "Emergency", "Voluntary"}'
);

INSERT INTO configuration_items
(
 configuration_item_id	         
,item_name                 
,business_unit_id          
,item_value                          
,item_values                     
)
VALUES
(
 500000003
,'RATE_TYPES'
,24
,null
,'{"Community", "Welfare", "Business", "Local", "County", "Emergency", "Voluntary"}'
);
