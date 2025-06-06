/**
* OPAL Program
*
* MODULE      : add_columns_to_results.sql
*
* DESCRIPTION : Add enforcement-related columns to the RESULTS table
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 28/05/2025    C Cho        1.0         PO-1698 Add enforcement related columns to the RESULTS table
*
**/

-- Add new columns to RESULTS table for enforcement action configuration
ALTER TABLE results
ADD COLUMN allow_payment_terms BOOLEAN,
ADD COLUMN requires_employment_data BOOLEAN,
ADD COLUMN allow_additional_action BOOLEAN,
ADD COLUMN enf_next_permitted_actions VARCHAR(100),
ADD COLUMN requires_lja BOOLEAN,
ADD COLUMN manual_enforcement BOOLEAN;

-- Add comments to new columns
COMMENT ON COLUMN results.allow_payment_terms IS 'Flag to state which enforcement actions allow the user to add/amend payment terms in the same journey as applying the action.';
COMMENT ON COLUMN results.requires_employment_data IS 'Flag to state that the enforcement action requires employment data to exist on the account in order to apply the action.';
COMMENT ON COLUMN results.allow_additional_action IS 'Flag to state which enforcement actions allow the user to add another enforcement action in the same journey as applying the action (WDN) or removing the action (NOENF).';
COMMENT ON COLUMN results.enf_next_permitted_actions IS 'A comma separated list of permitted next actions of result_ids for each active manual enforcement action. If All then allow all result_ids.';
COMMENT ON COLUMN results.requires_lja IS 'Flag to state that the enforcement override requires an LJA to be selected.';
COMMENT ON COLUMN results.manual_enforcement IS 'Flag to state that the result can be used for manual enforcement';