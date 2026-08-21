/**
* OPAL Program
*
* MODULE      : alter_business_units_business_unit_code_not_null_unique.sql
*
* DESCRIPTION : Make business_units.business_unit_code mandatory and unique.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------------------------
* 07/08/2026    TMc            1.0         PO-6322 - Add unique constraint to BUSINESS_UNITS.BUSINESS_UNIT_CODE
*
**/

ALTER TABLE business_units
    ALTER COLUMN business_unit_code SET NOT NULL,
    ADD CONSTRAINT bu_business_unit_code_uk UNIQUE (business_unit_code);