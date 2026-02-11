/**
* OPAL Program
*
* MODULE      : alter_enforcements_for_account_types.sql
*
* DESCRIPTION : Amend ENFORCEMENTS for auto enforcement account types.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------
* 28/01/2026    C Cho       1.0         PO-2455 Amend ENFORCEMENTS table for auto enforcement account types.
*
**/

ALTER TABLE enforcements
    DROP COLUMN IF EXISTS account_type;

ALTER TABLE enforcements
    ADD COLUMN enforcement_account_type t_enforcement_account_type_enum;

COMMENT ON COLUMN enforcements.enforcement_account_type IS 'The column is to hold only the values uniquely enumerated for the enforcement_account_types.enforcement_account_type column, e.g. ''COLL'', ''COLH'', ''AL'', ''AH'', ''COL'', ''COH'', ''YL'', ''YH''.';
