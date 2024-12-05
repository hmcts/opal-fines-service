/**
* OPAL Program
*
* MODULE      : insert_user_defined_error_messages.sql
*
* DESCRIPTION : This will load the user defined error messages that have initially been identified in the stored procedure design for Manual Account Creation
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------
* 01/11/2024    A Dennis    1.0         PO-897 This will load the user defined error messages that have initially been identified in the stored procedure design for Manual Account Creation
*
**/

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20001'
,'User not found'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20002'
,'Missing parent/guardian'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20003'
,'Invalid payment terms'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20004'
,'Result ID is not valid'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20005'
,'Missing creditor '
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20006'
,'Creditor ID not found'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20007'
,'Enforcement court ID not found'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20008'
,'Imposing court ID not found'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20009'
,'Offence ID not found'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20010'
,'Missing bank detail'
);

INSERT INTO error_messages
(
 error_code
,error_message
)
VALUES
(
 '-20011'
,'Missing ticket number'
);
