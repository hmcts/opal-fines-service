/**
* CGI OPAL Program
*
* MODULE      : alter_prosecutors.sql
*
* DESCRIPTION : Change the PROSECUTORS table ADDRESS_LINE_1 column size to VARCHAR(60)
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------------------------------------------------------------
* 09/10/2025    P Brumby    1.1         PO-1722 - Change the PROSECUTORS table to increase the column ADDRESS_LINE_1 size to VARCHAR(60) to hold reference data
*
**/

ALTER TABLE prosecutors
ALTER COLUMN address_line_1 TYPE VARCHAR(60);