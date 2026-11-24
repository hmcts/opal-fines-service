/**
* OPAL Program
*
* MODULE      : alter_local_justice_areas_for_ref_data_1_ROLLBACK.sql
*
* DESCRIPTION : Rollback script for: Alter LOCAL_JUSTICE_AREAS table (drop lja_code and set lja_type to not null)
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 24/11/2026    T McCallion    1.0         PO-10732 - Alter LOCAL_JUSTICE_AREAS table - ROLLBACK
*
**/

--lja_code column was never populated so should be left as NULL. 
ALTER TABLE local_justice_areas 
   ADD COLUMN IF NOT EXISTS lja_code VARCHAR(4) NULL,
   ALTER COLUMN lja_type DROP NOT NULL;
   
COMMENT ON COLUMN local_justice_areas.lja_code IS 'LJA Code';
