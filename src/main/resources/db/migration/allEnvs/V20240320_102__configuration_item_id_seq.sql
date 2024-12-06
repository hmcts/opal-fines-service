/**
* OPAL Program
*
* MODULE      : configuration_item_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table CONFIGURATION_ITEMS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 20/03/2024    A Dennis    1.0         PO-227 Creates the Sequence to be used to generate the Primary key for the table CONFIGURATION_ITEMS
*
**/
CREATE SEQUENCE IF NOT EXISTS configuration_item_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY configuration_items.configuration_item_id;
