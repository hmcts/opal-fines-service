/**
* OPAL Program
*
* MODULE      : add_local_justice_area_constraint.sql
*
* DESCRIPTION : Adds the foreign key constraint between courts and local_justice_areas tables
*               to ensure referential integrity
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 07/05/2025    C Cho        1.0         PO-938 Add constraint to ensure referential integrity between courts and local justice areas
*
**/

ALTER TABLE courts 
DROP CONSTRAINT IF EXISTS crt_local_justice_area_id_fk;

ALTER TABLE courts ADD CONSTRAINT crt_local_justice_area_id_fk 
FOREIGN KEY (local_justice_area_id) REFERENCES local_justice_areas(local_justice_area_id);