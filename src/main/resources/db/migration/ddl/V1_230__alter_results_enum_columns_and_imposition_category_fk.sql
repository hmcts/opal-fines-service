/**
* OPAL Program
*
* MODULE      : alter_results_enum_columns_and_imposition_category_fk.sql
*
* DESCRIPTION : Alter results enum-backed columns and imposition category foreign key
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3846 - Update columns on RESULTS table to use PostgreSQL ENUM and add FK to imposition_category
*
**/

ALTER TABLE results
    DROP CONSTRAINT IF EXISTS results_result_type_cc,
    DROP CONSTRAINT IF EXISTS results_imposition_creditor_cc,
    DROP CONSTRAINT IF EXISTS results_imposition_category_cc;

ALTER TABLE results
    ALTER COLUMN result_type TYPE t_result_type_enum
        USING result_type::text::t_result_type_enum,
    ALTER COLUMN imposition_creditor TYPE t_imposition_creditor_enum
        USING imposition_creditor::text::t_imposition_creditor_enum;

COMMENT ON COLUMN results.result_type IS 'Indicates if this is an actual result or just an action. Hard-coded in legacy GoB. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN results.imposition_creditor IS 'Indicates the creditor to be used for the imposition. Hard-coded in legacy GoB. Specific values can be found in the DB LLD on Confluence.';

ALTER TABLE results
    ADD CONSTRAINT results_imposition_category_fk FOREIGN KEY (imposition_category)
    REFERENCES imposition_categories (imposition_category);

CREATE INDEX results_imposition_category_idx ON results (imposition_category);
