/**
* CGI OPAL Program
*
* MODULE      : increase_offence_title_columns.sql
*
* DESCRIPTION : Increase the size of the offence_title and offence_title_cy columns from 120 to 1000 to hold those over 120 characters long
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------------------------------------------
* 18/09/2026    A Dennis    1.0         PO-10723 - R1A Go Live - Increase the size of the offence_title and offence_title_cy columns from 120 to 1000 to hold those over 120 characters long
*
*
**/
ALTER TABLE offences
    ALTER COLUMN offence_title TYPE VARCHAR(1000),
    ALTER COLUMN offence_title_cy TYPE VARCHAR(1000);
