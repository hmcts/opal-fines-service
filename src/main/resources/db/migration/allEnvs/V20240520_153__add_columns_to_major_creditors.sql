/**
* OPAL Program
*
* MODULE      : add_columns_to_major_creditors.sql
*
* DESCRIPTION : Add a new columns to the MAJOR_CREDITORS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 20/05/2024    A Dennis    1.0         PO-319 and 348 Add new columns to the MAJOR_CREDITORS table for contact details and so in order to be able to load Reference Data from data held in Excel spreadsheet.
*
**/
ALTER TABLE major_creditors
ADD COLUMN contact_name        varchar(35),
ADD COLUMN contact_telephone   varchar(35),
ADD COLUMN contact_email       varchar(80);

COMMENT ON COLUMN major_creditors.contact_name IS 'Holds a named individual';
COMMENT ON COLUMN major_creditors.contact_telephone IS 'Holds the telephone number for the major_creditor';
COMMENT ON COLUMN major_creditors.contact_email IS 'Holds the email address for the major_creditor';

ALTER TABLE major_creditors
ALTER COLUMN address_line_1 TYPE varchar(80);
