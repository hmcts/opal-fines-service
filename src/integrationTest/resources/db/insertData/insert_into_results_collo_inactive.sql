DELETE FROM result_documents
WHERE result_id = 'COLLO';

DELETE FROM enforcements
WHERE result_id = 'COLLO';

DELETE FROM results
WHERE result_id = 'COLLO';

INSERT INTO results
(
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
    result_parameters,
    allow_payment_terms,
    requires_employment_data,
    allow_additional_action,
    enf_next_permitted_actions,
    requires_lja,
    manual_enforcement
)
VALUES
    (
        'COLLO',
        'Payment terms enforcement test result',
        'Canlyniad prawf termau talu',
        'Action',
        FALSE,
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
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        NULL,
        FALSE,
        NULL,
        FALSE,
        NULL,
        NULL,
        FALSE
    );
