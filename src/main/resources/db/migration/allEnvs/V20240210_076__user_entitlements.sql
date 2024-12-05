/**
* CGI OPAL Program
*
* MODULE      : user_entitlements.sql
*
* DESCRIPTION : Creates the USER ENTITLEMENTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 11/02/2024    A Dennis    1.0         PO-177 Creates the USER ENTITLEMENTS table for the Fines model
*
**/
CREATE TABLE user_entitlements 
(
 user_entitlement_id       bigint        not null
,business_unit_user_id     varchar(6)    not null
,application_function_id   bigint        not null
,CONSTRAINT user_entitlements_pk PRIMARY KEY 
 (
   user_entitlement_id	
 ) 
);

ALTER TABLE user_entitlements
ADD CONSTRAINT ue_business_unit_user_id_fk FOREIGN KEY
(
  business_unit_user_id 
)
REFERENCES business_unit_users
(
  business_unit_user_id 
);

ALTER TABLE user_entitlements
ADD CONSTRAINT ue_application_function_id_fk FOREIGN KEY
(
  application_function_id 
)
REFERENCES application_functions
(
  application_function_id 
);

COMMENT ON COLUMN user_entitlements.user_entitlement_id IS 'Unique ID of this record';
COMMENT ON COLUMN user_entitlements.business_unit_user_id IS 'ID of the business unit user this record is for';
COMMENT ON COLUMN user_entitlements.application_function_id IS 'ID of the application function the business unit user can access';
