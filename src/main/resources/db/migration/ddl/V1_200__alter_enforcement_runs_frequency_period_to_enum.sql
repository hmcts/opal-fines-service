/**
* OPAL Program
*
* MODULE      : alter_enforcement_runs_frequency_period_to_enum.sql
*
* DESCRIPTION : Alter enforcement_runs.frequency_period to use PostgreSQL enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3623 - Update columns on ENFORCEMENT_RUNS table to use PostgreSQL ENUM
*
**/

ALTER TABLE enforcement_runs
    ALTER COLUMN frequency_period TYPE t_frequency_period_enum
    USING frequency_period::t_frequency_period_enum;

COMMENT ON COLUMN enforcement_runs.frequency_period IS 'How often the run will be initiated. Specific values can be found in the DB LLD on Confluence.';
