/**
* OPAL Program
*
* MODULE      : alter_results_imposition_category.sql
*
* DESCRIPTION : Alter column results.imposition_categories; change to Enum and drop related FK
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 10/11/2026    T McCallion    1.0         PO-11045 - Amend RESULTS table and p_get_creditor_account stored procedure
*
**/

ALTER TABLE results
    DROP CONSTRAINT IF EXISTS results_imposition_category_fk;

DROP INDEX IF EXISTS results_imposition_category_idx;

ALTER TABLE results
    ALTER COLUMN imposition_category TYPE t_imposition_category_enum
        USING imposition_category::TEXT::t_imposition_category_enum;