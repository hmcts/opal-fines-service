/**
* OPAL Program
*
* MODULE      : default_cred_trans_period_conf_item.sql
*
* DESCRIPTION : Inserts a row into the configuration_items table to provide a default value for the number of days before creditor transfer payments are considered cleared.
*               Used in Manual Account Creation
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------------------
* 11/08/2025    C Larkin    1.0         PO-2000 - Inserts a row into the configuration_items table to provide a default value for the number of days before creditor transfer payments are considered 
*                                       cleared.
*
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
   ,  'DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD'
   ,  NULL
   ,  0
   ,  NULL
);