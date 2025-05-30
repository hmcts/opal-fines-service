/**
* OPAL Program
*
* MODULE      : drop_local_justice_area_constraint.sql
*
* DESCRIPTION : Drops the foreign key constraint between courts and local_justice_areas tables
*               to allow updating local_justice_areas data
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 24/04/2025    C Cho        1.0         PO-1130 Drop constraint to enable local justice areas data update
*
**/

ALTER TABLE courts 
DROP CONSTRAINT IF EXISTS crt_local_justice_area_id_fk;