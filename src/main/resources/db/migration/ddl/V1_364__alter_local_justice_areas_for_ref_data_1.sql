/**
* OPAL Program
*
* MODULE      : alter_local_justice_areas_for_ref_data_1.sql
*
* DESCRIPTION : Alter LOCAL_JUSTICE_AREAS table (drop lja_code and set lja_type to not null)
*               Add values (SCDCRT, YTC) to t_lja_type_enum 
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 30/10/2026    T McCallion    1.0         PO-10732 - Alter LOCAL_JUSTICE_AREAS table
*
**/

ALTER TYPE t_lja_type_enum ADD VALUE IF NOT EXISTS 'SCDCRT';
ALTER TYPE t_lja_type_enum ADD VALUE IF NOT EXISTS 'YTC';

ALTER TABLE local_justice_areas 
    DROP COLUMN lja_code,
    ALTER COLUMN lja_type SET NOT NULL;  --Existing data does not contain NULLS