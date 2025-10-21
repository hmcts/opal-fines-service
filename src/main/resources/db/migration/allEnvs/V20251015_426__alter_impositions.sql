/**
* CGI OPAL Program
*
* MODULE      : alter_impositions.sql
*
* DESCRIPTION : Remove originator_name column from the impositsions table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------
* 13/10/2025    CL          1.0         PO-2291 - Remove originator_name column from the impositions table 
*
**/

-- Remove originator_name column from the impositions table
ALTER TABLE impositions DROP COLUMN originator_name;

