/**
* OPAL Program
*
* MODULE      : amend_enforcement_account_types.sql
*
* DESCRIPTION : Amend ENFORCEMENT_ACCOUNT_TYPES for auto enforcement configuration.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------
* 27/01/2026    C Cho       1.0         PO-2454 Amend ENFORCEMENT_ACCOUNT_TYPES table for auto enforcement configuration.
*
**/

CREATE TYPE t_account_type_enum AS ENUM ('COL', 'A', 'CO', 'Y');
CREATE TYPE t_enforcement_account_type_enum AS ENUM ('COLL', 'COLH', 'AL', 'AH', 'COL', 'COH', 'YL', 'YH');
CREATE TYPE t_low_high_value_enum AS ENUM ('L', 'H');

ALTER TABLE enforcement_account_types
    DROP CONSTRAINT IF EXISTS eat_business_unit_id_fk;

DROP INDEX IF EXISTS eat_business_unit_id_idx;

ALTER TABLE enforcement_account_types
    DROP COLUMN IF EXISTS business_unit_id,
    DROP COLUMN IF EXISTS account_type_name;

ALTER TABLE enforcement_account_types
    ALTER COLUMN account_type TYPE t_account_type_enum USING account_type::t_account_type_enum;

ALTER TABLE enforcement_account_types
    ADD COLUMN enforcement_account_type t_enforcement_account_type_enum NOT NULL,
    ADD COLUMN account_type_path t_low_high_value_enum,
    ADD COLUMN version_number bigint;

CREATE UNIQUE INDEX eat_enforcement_account_type_udx
    ON enforcement_account_types (enforcement_account_type);

COMMENT ON COLUMN enforcement_account_types.account_type IS 'The column is to hold only the values enumerated for the enforcement_account_types.account_type column, e.g. ''COL'', ''A'', ''CO'', ''Y'', and therefore a separate account_types table is not required.';
COMMENT ON COLUMN enforcement_account_types.enforcement_account_type IS 'The column is to hold only the values uniquely enumerated for the enforcement_account_types.enforcement_account_type column, e.g. ''COLL'', ''COLH'', ''AL'', ''AH'', ''COL'', ''COH'', ''YL'', ''YH''.';
COMMENT ON COLUMN enforcement_account_types.account_type_path IS 'The column is to hold only the values enumerated for enforcement_account_types.account_type_path column, i.e. either ''L'' or ''H'' representing either a ''low'' or ''high'' account type value respectively.';
COMMENT ON COLUMN enforcement_account_types.version_number IS 'The column is to be used to check that related items have not changed since retrieval and prior to being amended.';
