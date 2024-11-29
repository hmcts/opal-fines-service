/**
* OPAL Program
*
* MODULE      : error_messages.sql
*
* DESCRIPTION : Create the ERROR_MESSAGES table.
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    -------------------------------------
* 10/10/2024    I Readman    1.0         PO-666 Create the ERROR_MESSAGES table
*
**/

CREATE TABLE error_messages
(
 error_code           varchar(25)         not null
,error_message        varchar(1000)       not null
,CONSTRAINT em_error_code_pk PRIMARY KEY (error_code)
);

COMMENT ON COLUMN error_messages.error_code IS 'Unique ID of the user defined error message';
COMMENT ON COLUMN error_messages.error_message IS 'Descriptive wording of the error message';
