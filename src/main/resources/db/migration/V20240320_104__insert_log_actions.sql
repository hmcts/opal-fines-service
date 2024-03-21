/**
* OPAL Program
*
* MODULE      : insert_log_actions.sql
*
* DESCRIPTION : Inserts rows of data into the LOG_ACTIONS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 20/03/2024    A Dennis    1.0         PO-227 Inserts rows of data into the LOG_ACTIONS table
*
**/
INSERT INTO log_actions
(
 log_action_id	         
,log_action_name                                     
)
VALUES
(
 5000
,'Log In'
);

INSERT INTO log_actions
(
 log_action_id	         
,log_action_name                                     
)
VALUES
(
 5001
,'Log Out'
);

INSERT INTO log_actions
(
 log_action_id	         
,log_action_name                                     
)
VALUES
(
 5002
,'Account Notes'
);

INSERT INTO log_actions
(
 log_action_id	         
,log_action_name                                     
)
VALUES
(
 5004
,'Account Enquiry - Account Notes'
);
