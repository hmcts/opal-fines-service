/**
* OPAL Program
*
* MODULE      : add_lja_type_column.sql
*
* DESCRIPTION : Add lja_type column to LOCAL_JUSTICE_AREAS table
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 22/04/2025    C Cho        1.0         PO-1130 Add lja_type column to LOCAL_JUSTICE_AREAS table
*
**/

ALTER TABLE local_justice_areas
ADD COLUMN lja_type VARCHAR(50) NULL;

COMMENT ON COLUMN local_justice_areas.lja_type IS 'Type of local justice area';

