/**
* OPAL Program
*
* MODULE      : default_cheq_period_conf_item.sql
*
* DESCRIPTION : Inserts a row into the configuration_items table to provide a default value for number of days before a cheque is considered cleared.
*               Used in Manual Account Creation 
*               
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------------------
* 08/08/2025    C Larkin    1.0         PO-1103 - Inserts a row into the configuration_items table to provide a default value for the number of days before a cheque is considered cleared.

**/

INSERT INTO configuration_items 
   (
      configuration_item_id
   ,  item_name
   ,  business_unit_id
   ,  item_value
   ,  item_values
   ) 
VALUES 
   (
      nextval('configuration_item_id_seq')
   ,  'DEFAULT_CHEQUE_CLEARANCE_PERIOD'
   ,  NULL
   ,  10
   ,  NULL
);
