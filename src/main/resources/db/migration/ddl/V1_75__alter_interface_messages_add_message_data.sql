/**
* OPAL Program
*
* MODULE      : alter_interface_messages_add_message_data.sql
*
* DESCRIPTION : Add message data JSON to INTERFACE_MESSAGES.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 10/06/2026    C Cho       1.0         PO-2581 Add MESSAGE_DATA to INTERFACE_MESSAGES.
*
**/

ALTER TABLE interface_messages
    ADD COLUMN message_data JSON;

COMMENT ON COLUMN interface_messages.message_data IS 'This will hold the data, in the form a json, for the fields required to create the Till Summary screen.';
