/**
* CGI OPAL Program
*
* MODULE      : log_audit_details.sql
*
* DESCRIPTION : Creates the LOG_AUDIT_DETAILS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 20/03/2024    A Dennis    1.0         PO-227 Creates the LOG_AUDIT_DETAILS table for the Fines model
*
**/
CREATE TABLE log_audit_details 
(
 log_audit_detail_id     bigint          not null
,user_id                 bigint          not null
,log_timestamp           timestamp       not null
,log_action_id           smallint        not null
,account_number          varchar(20)
,business_unit_id        smallint
,json_request            text            not null
,CONSTRAINT log_audit_details_pk PRIMARY KEY 
 (
   log_audit_detail_id	
 ) 
);

ALTER TABLE log_audit_details
ADD CONSTRAINT lad_user_id_fk FOREIGN KEY
(
  user_id 
)
REFERENCES users
(
  user_id 
);

ALTER TABLE log_audit_details
ADD CONSTRAINT lad_log_action_id_fk FOREIGN KEY
(
  log_action_id 
)
REFERENCES log_actions
(
  log_action_id 
);

ALTER TABLE log_audit_details
ADD CONSTRAINT lad_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN log_audit_details.log_audit_detail_id IS 'Unique ID of this record';
COMMENT ON COLUMN log_audit_details.user_id IS 'The user whose actions led to the creation of this entry';
COMMENT ON COLUMN log_audit_details.log_timestamp IS 'System timestamp at the time of this entry';
COMMENT ON COLUMN log_audit_details.log_action_id IS 'The action that led to the creation of this entry';
COMMENT ON COLUMN log_audit_details.account_number IS 'The account related to this action if there is one';
COMMENT ON COLUMN log_audit_details.business_unit_id IS 'The business unit if there is one';
COMMENT ON COLUMN log_audit_details.json_request IS 'The REST request information received that initiated this action and written in a json format but stored as TEXT';
