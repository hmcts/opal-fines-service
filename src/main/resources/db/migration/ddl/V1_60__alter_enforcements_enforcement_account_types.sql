/**
* OPAL Program
*
* MODULE      : alter_enforcements_enforcement_account_types.sql
*
* DESCRIPTION : Alter tables ENFORCEMENTS and ENFORCEMENT_ACCOUNT_TYPES
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -----------------------------------------------------------------
* 10/06/2026    T McCallion    1.0         PO-6447 - Alter ENFORCEMENTS and ENFORCEMENT_ACCOUNT_TYPES tables
*
**/

CREATE TYPE t_enforcement_account_type_extended_enum AS ENUM ('COLL', 'COLH', 'AL', 'AH', 'COL', 'COH', 'YL', 'YH', 
                                                              'CFPL', 'CFPH', 'TFOL', 'TFOH', 'CCL', 'CCH', 'FPVL', 
                                                              'FPVH', 'FPNL', 'FPNH', 'LAL', 'LAH', 'COMP');

ALTER TABLE enforcements 
    ALTER COLUMN enforcement_account_type TYPE t_enforcement_account_type_extended_enum
        USING enforcement_account_type::TEXT::t_enforcement_account_type_extended_enum;


ALTER TABLE enforcement_account_types 
    ALTER COLUMN minimum_balance TYPE DECIMAL(18,2),
    ALTER COLUMN account_type_path SET NOT NULL;
