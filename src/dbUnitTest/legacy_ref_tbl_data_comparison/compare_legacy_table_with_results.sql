/**
* OPAL Program
*
* MODULE      : compare_legacy_table_with_results.sql
*
* DESCRIPTION : Compare data in the RESULTS_LEGACY table with the data in the RESULTS table.
*               Expected Results: No records returned = Data in both tables are identical. 
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ---------------------------------------------------
* 19/10/2026    T McCallion    1.0         PO-10770 - Create _legacy R1A reference data tables
*
**/
(
    SELECT 'Original' AS SOURCE
         , o.result_id, o.result_title, o.result_title_cy, o.result_type, o.active, o.imposition, o.imposition_category, o.imposition_allocation_priority
         , o.imposition_accruing, o.imposition_creditor, o.enforcement, o.enforcement_override, o.further_enforcement_warn, o.further_enforcement_disallow
         , o.enforcement_hold, o.requires_enforcer, o.generates_hearing, o.generates_warrant, o.collection_order, o.extend_ttp_disallow, o.extend_ttp_preserve_last_enf
         , o.prevent_payment_card, o.lists_monies, o.allow_payment_terms, o.requires_employment_data, o.allow_additional_action, o.enf_next_permitted_actions
         , o.requires_lja, o.manual_enforcement
         , o.result_parameters::TEXT AS result_parameters 
    FROM results AS o
    EXCEPT
    SELECT 'Original' AS SOURCE
         , l.result_id, l.result_title, l.result_title_cy, l.result_type, l.active, l.imposition, l.imposition_category, l.imposition_allocation_priority
         , l.imposition_accruing, l.imposition_creditor, l.enforcement, l.enforcement_override, l.further_enforcement_warn, l.further_enforcement_disallow
         , l.enforcement_hold, l.requires_enforcer, l.generates_hearing, l.generates_warrant, l.collection_order, l.extend_ttp_disallow, l.extend_ttp_preserve_last_enf
         , l.prevent_payment_card, l.lists_monies, l.allow_payment_terms, l.requires_employment_data, l.allow_additional_action, l.enf_next_permitted_actions
         , l.requires_lja, l.manual_enforcement
         , l.result_parameters::TEXT AS result_parameters 
    FROM results_legacy AS l
)
UNION ALL
(
    SELECT 'Legacy' AS SOURCE
         , l.result_id, l.result_title, l.result_title_cy, l.result_type, l.active, l.imposition, l.imposition_category, l.imposition_allocation_priority
         , l.imposition_accruing, l.imposition_creditor, l.enforcement, l.enforcement_override, l.further_enforcement_warn, l.further_enforcement_disallow
         , l.enforcement_hold, l.requires_enforcer, l.generates_hearing, l.generates_warrant, l.collection_order, l.extend_ttp_disallow, l.extend_ttp_preserve_last_enf
         , l.prevent_payment_card, l.lists_monies, l.allow_payment_terms, l.requires_employment_data, l.allow_additional_action, l.enf_next_permitted_actions
         , l.requires_lja, l.manual_enforcement
         , l.result_parameters::TEXT AS result_parameters 
    FROM results_legacy AS l
    EXCEPT
    SELECT 'Legacy' AS SOURCE
         , o.result_id, o.result_title, o.result_title_cy, o.result_type, o.active, o.imposition, o.imposition_category, o.imposition_allocation_priority
         , o.imposition_accruing, o.imposition_creditor, o.enforcement, o.enforcement_override, o.further_enforcement_warn, o.further_enforcement_disallow
         , o.enforcement_hold, o.requires_enforcer, o.generates_hearing, o.generates_warrant, o.collection_order, o.extend_ttp_disallow, o.extend_ttp_preserve_last_enf
         , o.prevent_payment_card, o.lists_monies, o.allow_payment_terms, o.requires_employment_data, o.allow_additional_action, o.enf_next_permitted_actions
         , o.requires_lja, o.manual_enforcement
         , o.result_parameters::TEXT AS result_parameters 
    FROM results AS o
)
ORDER BY result_id, source;