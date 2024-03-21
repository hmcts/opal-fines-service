/**
* CGI OPAL Program
*
* MODULE      : log_actions.sql
*
* DESCRIPTION : Creates the LOG_ACTIONS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 20/03/2024    A Dennis    1.0         PO-227 Creates the LOG_ACTIONS table for the Fines model
*
**/
CREATE TABLE log_actions 
(
 log_action_id       smallint          not null
,log_action_name     varchar(200)      not null
,CONSTRAINT log_actions_pk PRIMARY KEY 
 (
   log_action_id	
 ) 
);

COMMENT ON COLUMN log_actions.log_action_id IS 'Unique ID of this record';
COMMENT ON COLUMN log_actions.log_action_name IS 'The description of actions that could give rise to log creation';
