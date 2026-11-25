/**
* OPAL Program
*
* MODULE      : alter_prisons_for_ref_data_2.sql
*
* DESCRIPTION : Prepare Prisons for the national reference dataset and restart its identifier sequence.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ---------------------------------------------
* 10/11/2026    T McCallion    1.0         PO-11046 - Amend PRISONS table for Ref Data 2
*
**/

ALTER TABLE prisons
    DROP CONSTRAINT IF EXISTS pri_business_unit_id_fk,
    DROP COLUMN IF EXISTS business_unit_id,
    DROP COLUMN IF EXISTS prison_code,
    ALTER COLUMN name TYPE VARCHAR(200),
    ALTER COLUMN address_line_1 DROP NOT NULL,
    ADD COLUMN address_line_4 VARCHAR(35),
    ADD COLUMN address_line_5 VARCHAR(35),
    ADD CONSTRAINT pri_name_uk UNIQUE (name);

COMMENT ON COLUMN prisons.address_line_4 IS 'Prison address line 4';
COMMENT ON COLUMN prisons.address_line_5 IS 'Prison address line 5';