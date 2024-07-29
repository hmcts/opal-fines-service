/**
* OPAL Program
*
* MODULE      : insert_language_configuration_items.sql
*
* DESCRIPTION : Inserts rows of data into the CONFIGURATION_ITEMS table for Document and Hearing language. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 29/07/2024    A Dennis    1.0         PO-509 Inserts rows of data into the CONFIGURATION_ITEMS table for Document and Hearing language.
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
 500000001
,'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE'
,78    -- this is business unit for North Wales
,'W'
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
 500000002
,'DEFAULT_HEARING_LANGUAGE_PREFERENCE'
,78
,'W'
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
 500000003
,'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE'
,58     -- this is business_unit for Gwent
,'W'
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
 500000004
,'DEFAULT_HEARING_LANGUAGE_PREFERENCE'
,58
,'W'
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
 500000005
,'DEFAULT_DOCUMENT_LANGUAGE_PREFERENCE'
,53     -- this is business_unit for Dyfed Powys
,'W'
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
 500000006
,'DEFAULT_HEARING_LANGUAGE_PREFERENCE'
,53
,'W'
,NULL
);
