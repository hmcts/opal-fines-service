/**
* OPAL Program
*
* MODULE      : insert_into_enforcements_entity_graph.sql
*
* DESCRIPTION : Inserts targeted enforcement entity graph test data for repository integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 16/04/2026  S WILLIAMS  1.0      PO-2883: Add enforcement data to verify lazy loading and named entity graph fetch behaviour.
*
*/

INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_type
) VALUES (
    910,
    'Entity Graph BU',
    'Accounting Division'
);

INSERT INTO local_justice_areas (
    local_justice_area_id,
    name,
    address_line_1
) VALUES (
    910,
    'Entity Graph LJA',
    '1 Graph Street'
);

INSERT INTO courts (
    court_id,
    business_unit_id,
    court_code,
    name,
    local_justice_area_id
) VALUES (
    910004,
    910,
    910,
    'Entity Graph Court',
    910
);

INSERT INTO enforcers (
    enforcer_id,
    business_unit_id,
    enforcer_code,
    name
) VALUES (
    910003,
    910,
    910,
    'Entity Graph Enforcer'
);

INSERT INTO results (
    result_id,
    result_title,
    result_title_cy,
    result_type,
    active,
    imposition,
    imposition_category,
    imposition_allocation_priority,
    imposition_accruing,
    imposition_creditor,
    enforcement,
    enforcement_override,
    further_enforcement_warn,
    further_enforcement_disallow,
    enforcement_hold,
    requires_enforcer,
    generates_hearing,
    generates_warrant,
    collection_order,
    extend_ttp_disallow,
    extend_ttp_preserve_last_enf,
    prevent_payment_card,
    lists_monies,
    manual_enforcement,
    allow_payment_terms,
    requires_employment_data,
    allow_additional_action,
    requires_lja,
    enf_next_permitted_actions,
    result_parameters
) VALUES (
    'ER9100',
    'Entity Graph Result',
    'Entity Graph Result',
    'Result',
    TRUE,
    FALSE,
    NULL,
    NULL,
    FALSE,
    NULL,
    TRUE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    TRUE,
    TRUE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    FALSE,
    NULL,
    NULL
);

INSERT INTO defendant_accounts (
    defendant_account_id,
    version_number,
    business_unit_id,
    account_number,
    amount_paid,
    account_balance,
    amount_imposed,
    allow_writeoffs,
    allow_cheques,
    account_type,
    collection_order,
    payment_card_requested,
    account_status,
    last_enforcement
) VALUES (
    910002,
    0,
    910,
    '910002A',
    0.00,
    250.00,
    250.00,
    'N',
    'N',
    'Fine',
    'N',
    'N',
    'L',
    'ER9100'
);

INSERT INTO enforcements (
    enforcement_id,
    defendant_account_id,
    posted_date,
    posted_by,
    result_id,
    reason,
    enforcer_id,
    jail_days,
    warrant_reference,
    case_reference,
    hearing_date,
    hearing_court_id,
    enforcement_account_type,
    posted_by_name,
    result_responses
) VALUES (
    910001,
    910002,
    TIMESTAMP '2026-04-01 09:30:00',
    'entity.graph',
    'ER9100',
    'Entity graph reason',
    910003,
    14,
    'WAR-910001',
    'CASE-910001',
    TIMESTAMP '2026-04-15 10:45:00',
    910004,
    'COLL',
    'Entity Graph User',
    '{"param":"value"}'
);
