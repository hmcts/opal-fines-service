/**
* OPAL Program
*
* MODULE      : insert_into_result_entity_graph.sql
*
* DESCRIPTION : Inserts targeted result entity graph test data for repository integration tests.
*
* VERSION HISTORY:
*
* Date        Author      Version  Nature of Change
* ----------  ----------  -------  -------------------------------------------------------------
* 27/04/2026  Dat Nguyen       1.0      Add result data to verify lazy loading and named entity graph fetch behaviour.
*
*/

INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_type
) VALUES (
    920,
    'Result Graph BU',
    'Accounting Division'
);

INSERT INTO local_justice_areas (
    local_justice_area_id,
    name,
    address_line_1
) VALUES (
    920,
    'Result Graph LJA',
    '1 Result Street'
);

INSERT INTO courts (
    court_id,
    business_unit_id,
    court_code,
    name,
    local_justice_area_id
) VALUES (
    920004,
    920,
    920,
    'Result Graph Court',
    920
);

INSERT INTO enforcers (
    enforcer_id,
    business_unit_id,
    enforcer_code,
    name
) VALUES (
    920003,
    920,
    920,
    'Result Graph Enforcer'
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
    'RG9200',
    'Result Graph Result',
    'Result Graph Result',
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
    'RG9201,RG9202',
    '{"param":"value"}'
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
    920002,
    0,
    920,
    '920002A',
    0.00,
    250.00,
    250.00,
    'N',
    'N',
    'Fine',
    'N',
    'N',
    'L',
    'RG9200'
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
    920001,
    920002,
    TIMESTAMP '2026-04-01 09:30:00',
    'result.graph',
    'RG9200',
    'Result graph reason',
    920003,
    14,
    'WAR-920001',
    'CASE-920001',
    TIMESTAMP '2026-04-15 10:45:00',
    920004,
    'COLL',
    'Result Graph User',
    '{"param":"value"}'
);
